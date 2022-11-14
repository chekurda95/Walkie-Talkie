package com.chekurda.main_screen.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_fragment.BasePresenterFragment
import com.chekurda.common.storeIn
import com.chekurda.main_screen.R
import com.chekurda.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.main_screen.data.DeviceInfo
import com.chekurda.main_screen.presentation.device_list.DeviceListAdapter
import com.chekurda.main_screen.presentation.device_list.holder.DeviceViewHolder
import com.chekurda.main_screen.utils.PermissionsHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    private val itemClickHandler = DeviceViewHolder.ActionListener {
        connect(it)
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: DeviceInfo) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.address
            wps.setup = WpsInfo.PBC
        }
        permissionsHelper.withPermissions {
            manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {}
                        override fun onFailure(reason: Int) {}
                    })
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(
                        this@MainScreenFragment.context,
                        "Connect failed. Retry.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun disconnect() {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                onDisconnected()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                    this@MainScreenFragment.context,
                    "Cancel connect failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private val permissionsHelper = PermissionsHelper(permissions, PERMISSIONS_REQUEST_CODE) { requireActivity() }
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var searchButton: Button? = null
    private var disconnectButton: Button? = null
    private var sayButton: Button? = null

    private val wifiDeviceSubject = PublishSubject.create<List<WifiP2pDevice>>()

    private val disposer = CompositeDisposable()

    private val devices = mutableListOf<DeviceInfo>()
    private var adapter: DeviceListAdapter = DeviceListAdapter(itemClickHandler)

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        wifiDeviceSubject.onNext(peerList.deviceList.toList())
    }

    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    @Volatile private var isConnected = false

    private lateinit var address: InetAddress
    private var groupOwner = false

    private var isWifiP2pEnabled = false
    private val wifiReceiver = object : BroadcastReceiver() {

        private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
            // String from WifiP2pInfo struct
            address = info.groupOwnerAddress
            groupOwner = info.isGroupOwner
            onConnected()
        }

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    permissionsHelper.withPermissions {
                        manager.requestPeers(channel, peerListListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        manager.requestConnectionInfo(channel, connectionListener)
                    } else {
                        onDisconnected()
                    }
                    Log.e("TAGTAG", "WIFI_P2P_CONNECTION_CHANGED_ACTION")
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.e("TAGTAG", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.device_list).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainScreenFragment.adapter
        }
        disconnectButton = view.findViewById<Button?>(R.id.disconnect_button).apply {
            setOnClickListener { disconnect() }
        }
        progress = view.findViewById(R.id.search_progress)
        searchButton = view.findViewById<Button?>(R.id.search_button).apply {
            setOnClickListener { startSearch() }
        }
        sayButton = view.findViewById<Button?>(R.id.say_button).apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isButtonPressed = true
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        isButtonPressed = false
                    }
                }
                false
            }
        }
        subscribeOnDevices()
        manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(requireActivity(), requireActivity().mainLooper, null)
    }

    @SuppressLint("MissingPermission")
    private fun startSearch() {
        permissionsHelper.withPermissions {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                }

                override fun onFailure(reasonCode: Int) {

                }
            })
        }
    }

    private fun subscribeOnDevices() {
        wifiDeviceSubject.subscribeOn(Schedulers.newThread())
            .distinctUntilChanged()
            .map { deviceList ->
                deviceList.map { device ->
                    DeviceInfo(
                        address = device.deviceAddress,
                        name = device.deviceName
                    )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { deviceInfo ->
                devices.clear()
                devices.addAll(deviceInfo)
                adapter.setDataList(devices.toList())
            }.storeIn(disposer)
    }

    override fun onStart() {
        super.onStart()
        permissionsHelper.request()
    }

    override fun onStop() {
        super.onStop()
        stopDiscovery()
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(wifiReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(wifiReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        disposer.clear()
    }

    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl()

    private fun stopDiscovery() {
        searchButton?.isEnabled = true
        progress?.isVisible = false
    }

    var track: AudioTrack? = null
    var recorder: AudioRecord? = null
    val minBufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)

    private fun onConnected() {
        isConnected = true
        disconnectButton?.isVisible = true
        sayButton?.isVisible = true

        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(16000)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_FRONT_LEFT)
            .build()
        track = AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(format)
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )
        startRecording()
        startPlaying()
    }

    private fun onDisconnected() {
        isConnected = false
        disconnectButton?.isVisible = false
        sayButton?.isVisible = false
    }

    private var playThread: Thread? = null
    private fun startPlaying() {
        track?.play()
        playThread = object : Thread() {

            lateinit var socket: Socket
            override fun run() {
                super.run()
                val buffer = ByteArray(minBufferSize)
                if (groupOwner) {
                    val serverSocket = ServerSocket(1234, 50, address)
                    socket = serverSocket.accept()
                } else {
                    socket = Socket()
                    socket.connect(InetSocketAddress(address, 1234), 500)
                }
                val inputStream = socket.getInputStream()
                val outputStream = socket.getOutputStream()
                while (isConnected) {
                    if (isButtonPressed) {
                        recorder!!.read(buffer, 0, buffer.size)
                        outputStream.write(buffer)
                    }
                    if (inputStream.available() == 0) continue
                    if (!isButtonPressed) {
                        inputStream.read(buffer)
                        track?.write(buffer, 0, buffer.size)
                    }
                }
                socket.close()
            }
        }.apply { start() }
    }

    @Volatile
    private var isButtonPressed: Boolean = false

    private fun startRecording() {
        recorder?.startRecording()
    }
}

private val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102