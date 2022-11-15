package com.chekurda.walkie_talkie.main_screen.presentation

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
import android.media.AudioTrack.PERFORMANCE_MODE_POWER_SAVING
import android.media.MediaRecorder
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
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
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.presentation.device_list.DeviceListAdapter
import com.chekurda.walkie_talkie.main_screen.utils.PermissionsHelper
import com.chekurda.walkie_talkie.main_screen.utils.RecordingDeviceHelper
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var searchButton: Button? = null
    private var disconnectButton: Button? = null
    private var sayButton: Button? = null

    private val permissionsHelper = PermissionsHelper(permissions, PERMISSIONS_REQUEST_CODE) { requireActivity() }
    private var deviceHelper: RecordingDeviceHelper? = null

    private val wifiDeviceSubject = BehaviorSubject.createDefault(emptyList<WifiP2pDevice>())
    private val devices = mutableListOf<DeviceInfo>()

    private var adapter: DeviceListAdapter = DeviceListAdapter(::connect)

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    var track: AudioTrack? = null
    var recorder: AudioRecord? = null
    val minBufferSize = AudioTrack.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private var isWifiP2pEnabled = false

    @Volatile
    private var isConnected: Boolean = false

    @Volatile
    private var isButtonPressed: Boolean = false

    private val wifiReceiver = object : BroadcastReceiver() {

        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    permissionsHelper.withPermissions {
                        manager.requestPeers(channel) { peerList ->
                            wifiDeviceSubject.onNext(peerList.deviceList.toList())
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        manager.requestConnectionInfo(channel, ::prepareSocket)
                    } else {
                        onDisconnected()
                    }
                }
            }
        }
    }

    private val searchDisposable = SerialDisposable()
    private val disposer = CompositeDisposable().apply {
        add(searchDisposable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deviceHelper = RecordingDeviceHelper(requireActivity())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.search_progress)
        recyclerView = view.findViewById<RecyclerView>(R.id.device_list).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainScreenFragment.adapter
            subscribeOnSearchDevices()
        }
        disconnectButton = view.findViewById<Button?>(R.id.disconnect_button).apply {
            setOnClickListener { disconnect() }
        }
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
        manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(requireActivity(), requireActivity().mainLooper, null)
    }

    override fun onStart() {
        super.onStart()
        deviceHelper?.configureDevice(isStartRecording = isConnected)
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(wifiReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(wifiReceiver)
    }

    override fun onStop() {
        super.onStop()
        deviceHelper?.configureDevice(isStartRecording = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        progress = null
        searchButton = null
        disconnectButton = null
        disposer.clear()
    }

    override fun onDetach() {
        super.onDetach()
        deviceHelper = null
    }

    private fun startSearch() {
        permissionsHelper.withPermissions {
            Observable.timer(15, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onSearchStarted() }
                .subscribe {
                    cancelSearch()
                }.storeIn(searchDisposable)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onSearchStarted() {
        permissionsHelper.withPermissions {
            manager.discoverPeers(channel, emptyManagerListener)
            searchButton?.isEnabled = false
            progress?.isVisible = true
        }
    }

    private fun cancelSearch() {
        searchDisposable.set(null)
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                searchButton?.isEnabled = true
                progress?.isVisible = false
            }
            override fun onFailure(reason: Int) = Unit
        })
    }

    private fun subscribeOnSearchDevices() {
        wifiDeviceSubject.distinctUntilChanged()
            .map { deviceList ->
                deviceList.map { device ->
                    DeviceInfo(
                        address = device.deviceAddress,
                        name = device.deviceName
                    )
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { deviceInfo ->
                devices.clear()
                devices.addAll(deviceInfo)
                adapter.setDataList(devices.toList())
            }.storeIn(disposer)
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
        disconnectButton?.isVisible = true
        sayButton?.isVisible = true
        prepareAudioTrack(socket)
        prepareAudioRecorder(socket)
        deviceHelper?.configureDevice(isStartRecording = true)
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

    private fun onDisconnected() {
        isConnected = false
        disconnectButton?.isVisible = false
        sayButton?.isVisible = false
        try {
            track?.release()
        } catch (ignore: Exception) { }
        try {
            recorder?.release()
        } catch (ignore: Exception) { }
        track = null
        recorder = null
        deviceHelper?.configureDevice(isStartRecording = false)
    }

    private fun prepareAudioTrack(socket: Socket) {
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
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setPerformanceMode(PERFORMANCE_MODE_POWER_SAVING)
                }
            }
            .build()
            .apply { play() }
        object : Thread() {
            val mainHandler = view?.handler
            override fun run() {
                super.run()
                val buffer = ByteArray(minBufferSize)
                val inputStream = socket.getInputStream()
                kotlin.runCatching {
                    while (isConnected) {
                        if (inputStream.available() == 0) continue
                        if (!isButtonPressed) {
                            inputStream.read(buffer)
                            track?.write(buffer, 0, buffer.size)
                        }
                    }
                }.apply { if (isFailure) mainHandler?.post { disconnect() } }
                socket.close()
            }
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun prepareAudioRecorder(socket: Socket) {
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        ).apply { startRecording() }
        object : Thread() {
            val mainHandler = view?.handler
            override fun run() {
                super.run()
                val buffer = ByteArray(minBufferSize)
                val outputStream = socket.getOutputStream()
                kotlin.runCatching {
                    while (isConnected) {
                        if (isButtonPressed) {
                            recorder!!.read(buffer, 0, buffer.size)
                            outputStream.write(buffer)
                        }
                    }
                }.apply { if (isFailure) mainHandler?.post { disconnect() } }
                socket.close()
            }
        }.apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl()
}

private val emptyManagerListener = object : WifiP2pManager.ActionListener {
    override fun onSuccess() = Unit
    override fun onFailure(reason: Int) = Unit
}

private val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102
private const val CONNECTION_PORT = 6436
private const val BACKLOG = 50
private const val CONNECTION_TIMEOUT = 500