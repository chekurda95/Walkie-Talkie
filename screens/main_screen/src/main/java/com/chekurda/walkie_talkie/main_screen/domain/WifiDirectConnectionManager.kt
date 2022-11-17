package com.chekurda.walkie_talkie.main_screen.domain

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.util.Log
import com.chekurda.common.storeIn
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
internal class WifiDirectConnectionManager(
    private val audioStreamer: AudioStreamer
) {
    interface ProcessListener {
        fun onPeersChanged(devices: List<WifiP2pDevice>)
        fun onConnectionSuccess(device: WifiP2pDevice)
        fun onConnectionCanceled(isError: Boolean)
        fun onSearchStateChanged(isRunning: Boolean)
        fun onWaitingConnection()
    }

    var processListener: ProcessListener? = null

    @Volatile
    private var isConnected: Boolean = false

    private var channel: WifiP2pManager.Channel? = null
    private var manager: WifiP2pManager? = null

    private val connectDisposable = SerialDisposable()
    private val prepareSocketDisposable = SerialDisposable()
    private val disposer = CompositeDisposable().apply {
        add(connectDisposable)
        add(prepareSocketDisposable)
    }
    private var isGroupConnected = false
    private var deviceList: List<WifiP2pDevice> = emptyList()
    private var connectedDeviceAddress: String? = null

    private val wifiReceiver = WifiDirectReceiver(
        object : WifiDirectReceiver.ProcessListener {

            override fun onPeersChanged() {
                manager?.requestPeers(channel) { peerList ->
                    if (deviceList.isNotEmpty()) deviceList = peerList.deviceList.toList()
                    processListener?.onPeersChanged(peerList.deviceList.toList())
                }
            }

            override fun onConnectionChanged(isConnected: Boolean) {
                if (isConnected) {
                    manager?.requestConnectionInfo(channel, ::prepareSocket)
                } else if (isGroupConnected) {
                    disconnect(isError = true)
                }
                isGroupConnected = isConnected
            }
        }
    )

    var amplitudeListener: AudioStreamer.AmplitudeListener?
        get() = audioStreamer.amplitudeListener
        set(value) {
            audioStreamer.amplitudeListener = value
        }

    fun init(activity: Activity) {
        manager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager!!.initialize(activity, activity.mainLooper, null)
    }

    fun clear() {
        disconnect()
        this.manager = null
        this.channel = null
        deviceList = emptyList()
        connectedDeviceAddress = null
    }

    fun startSearchDevices() {
        checkNotNull(manager).discoverPeers(channel, emptyManagerListener)
        processListener?.onSearchStateChanged(isRunning = true)
    }

    fun stopSearchDevices() {
        checkNotNull(manager).stopPeerDiscovery(channel, emptyManagerListener)
        processListener?.onSearchStateChanged(isRunning = false)
    }

    fun connect(address: String) {
        Log.e("TAGTAG", "try connect address $address")
        prepareSocketDisposable.set(null)
        connectDisposable.set(null)
        Observable.timer(CONNECTION_WAITING_TIMEOUT_SEC, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                checkNotNull(manager).connect(
                    channel,
                    WifiP2pConfig().apply {
                        deviceAddress = address
                        wps.setup = WpsInfo.PBC
                    },
                    object : ActionListener {
                        override fun onSuccess() {
                            connectDisposable.set(null)
                        }
                        override fun onFailure(reason: Int) {
                            connectDisposable.set(null)
                            onDisconnected(isError = true)
                        }
                    }
                )
                processListener?.onWaitingConnection()
            }
            .ignoreElements()
            .subscribe {
                prepareSocketDisposable.set(null)
                disconnect(isError = true)
            }.storeIn(connectDisposable)
    }

    fun disconnect(isError: Boolean = false, listener : ActionListener? = null) {
        Log.e("TAGTAG", "disconnect call")
        val callback = listener ?: object : ActionListener {
            override fun onSuccess() {
                onDisconnected(isError)
            }

            override fun onFailure(reason: Int) {
                onDisconnected(isError)
            }
        }
        manager?.removeGroup(channel, callback)
    }

    fun registerDirectListener(context: Context) {
        wifiReceiver.register(context)
    }

    fun unregisterDirectListener(context: Context) {
        wifiReceiver.unregister(context)
    }

    fun changeSteamDirection(isListening: Boolean) {
        audioStreamer.changeStreamDirection(isListening)
    }

    fun release() {
        clear()
        disposer.dispose()
        processListener = null
    }

    private fun prepareSocket(connectionInfo: WifiP2pInfo) {
        Single.fromCallable {
            var serverSocket: ServerSocket? = null
            var socket: Socket? = null
            try {
                if (connectionInfo.isGroupOwner) {
                    serverSocket = ServerSocket(CONNECTION_PORT, BACKLOG, connectionInfo.groupOwnerAddress)
                    socket = serverSocket.accept()
                } else {
                    val address = InetSocketAddress(connectionInfo.groupOwnerAddress, CONNECTION_PORT)
                    socket = Socket().apply { connect(address, CONNECTION_TIMEOUT) }
                }
            } catch (ex: Exception) {
                Log.e("TAGTAG","socket exception = $ex")
                socket?.close()
                serverSocket?.close()
            }
            if (socket == null) throw IllegalStateException()
            socket
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onConnected) {
                Log.e("TAGTAG", "prepareSocket error $it")
                disconnect(isError = true)
            }.storeIn(prepareSocketDisposable)
    }

    private fun onConnected(socket: Socket) {
        Log.e("TAGTAG", "onConnected")
        connectDisposable.set(null)
        isConnected = true
        val connectedDevice = deviceList.find { it.deviceAddress == connectedDeviceAddress }
            ?: WifiP2pDevice().apply {
                deviceAddress = connectedDeviceAddress.orEmpty()
                deviceName = "UNKNOWN"
            }
        processListener?.onConnectionSuccess(connectedDevice)
        audioStreamer.connect(socket) {
            disconnect(isError = false)
        }
    }

    private fun onDisconnected(isError: Boolean) {
        Log.e("TAGTAG", "onDisconnected isError = $isError")
        prepareSocketDisposable.set(null)
        isConnected = false
        isGroupConnected = false
        connectedDeviceAddress = null
        audioStreamer.disconnect()
        processListener?.onConnectionCanceled(isError = isError)
    }
}

private class WifiDirectReceiver(
    private val listener: ProcessListener
) : BroadcastReceiver() {

    interface ProcessListener {
        fun onPeersChanged()
        fun onConnectionChanged(isConnected: Boolean)
    }

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> listener.onPeersChanged()
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                Log.e("TAGTAG", "WIFI_P2P_CONNECTION_CHANGED_ACTION ${networkInfo?.isConnected == true}")
                listener.onConnectionChanged(networkInfo?.isConnected == true)
            }
        }
    }

    fun register(context: Context) {
        context.registerReceiver(this, intentFilter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}

private val emptyManagerListener = object : ActionListener {
    override fun onSuccess() = Unit
    override fun onFailure(reason: Int) = Unit
}

private const val CONNECTION_WAITING_TIMEOUT_SEC = 25L
private const val CONNECTION_PORT = 6542
private const val BACKLOG = 50
private const val CONNECTION_TIMEOUT = 2000