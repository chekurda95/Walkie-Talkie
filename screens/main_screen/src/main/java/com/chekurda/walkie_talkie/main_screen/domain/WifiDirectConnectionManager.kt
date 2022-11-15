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
import com.chekurda.common.storeIn
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
        fun onConnectionResult(isSuccess: Boolean)
        fun onGroupConnectionStateChanged(isConnected: Boolean)
    }

    var processListener: ProcessListener? = null

    @Volatile
    private var isConnected: Boolean = false

    private var channel: WifiP2pManager.Channel? = null
    private var manager: WifiP2pManager? = null

    private val disposer = CompositeDisposable()

    private val wifiReceiver = WifiDirectReceiver(
        object : WifiDirectReceiver.ProcessListener {

            override fun onPeersChanged() {
                manager?.requestPeers(channel) { peerList ->
                    processListener?.onPeersChanged(peerList.deviceList.toList())
                }
            }

            override fun onConnectionChanged(isConnected: Boolean) {
                if (isConnected) manager?.requestConnectionInfo(channel, ::prepareSocket)
                processListener?.onGroupConnectionStateChanged(isConnected)
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
    }

    fun searchDevices(): Completable =
        Observable.timer(SEARCH_DEVICES_TIMEOUT_SEC, TimeUnit.SECONDS)
            .doOnSubscribe { checkNotNull(manager).discoverPeers(channel, emptyManagerListener) }
            .doOnComplete { checkNotNull(manager).stopPeerDiscovery(channel, emptyManagerListener) }
            .ignoreElements()

    fun connect(address: String) {
        checkNotNull(manager).connect(
            channel,
            WifiP2pConfig().apply {
                deviceAddress = address
                wps.setup = WpsInfo.PBC
            },
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() = Unit
                override fun onFailure(reason: Int) {
                    processListener?.onConnectionResult(isSuccess = false)
                }
            }
        )
    }

    fun disconnect(listener : WifiP2pManager.ActionListener? = null) {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                onDisconnected()
                listener?.onSuccess()
            }

            override fun onFailure(reason: Int) {
                listener?.onFailure(reason)
            }
        })
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
            val socket = if (connectionInfo.isGroupOwner) {
                val serverSocket = ServerSocket(CONNECTION_PORT, BACKLOG, connectionInfo.groupOwnerAddress)
                serverSocket.use { it.accept() }
            } else {
                val address = InetSocketAddress(connectionInfo.groupOwnerAddress, CONNECTION_PORT)
                Socket().use { it.apply { connect(address, CONNECTION_TIMEOUT) } }
            }
            if (!socket.isConnected) throw IllegalStateException()
            socket
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onConnected) { onDisconnected() }
            .storeIn(disposer)
    }

    private fun onConnected(socket: Socket) {
        isConnected = true
        processListener?.onConnectionResult(isSuccess = true)
        audioStreamer.connect(socket) {
            disconnect(object : WifiP2pManager.ActionListener {
                val callback = {  processListener?.onGroupConnectionStateChanged(isConnected = false) }
                override fun onSuccess() {
                    callback()
                }
                override fun onFailure(reason: Int) {
                    callback()
                }
            })
        }
    }

    private fun onDisconnected() {
        isConnected = false
        audioStreamer.disconnect()
        processListener?.onGroupConnectionStateChanged(isConnected = false)
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

private val emptyManagerListener = object : WifiP2pManager.ActionListener {
    override fun onSuccess() = Unit
    override fun onFailure(reason: Int) = Unit
}

private const val SEARCH_DEVICES_TIMEOUT_SEC = 15L
private const val CONNECTION_PORT = 6436
private const val BACKLOG = 50
private const val CONNECTION_TIMEOUT = 500