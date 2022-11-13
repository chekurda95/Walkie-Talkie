package com.chekurda.main_screen.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
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
import com.chekurda.main_screen.utils.SimpleReceiver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.Exception
import java.util.UUID

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    private val itemClickHandler = DeviceViewHolder.ActionListener {
        connect(it)
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var secureUUID = UUID.fromString(SECURE_UUID)

    private fun connect(device: DeviceInfo) {
        stopDiscovery()
        Single.fromCallable {
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)
            val socket = bluetoothDevice.createRfcommSocketToServiceRecord(secureUUID)
            try {
                socket.apply { connect() }
            } catch (ex: Exception) {
                socket.close()
                throw Exception()
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    bluetoothSocket = it
                    disconnectButton?.isVisible = true
                }, {
                    Log.e("connect", "${it.message}\n${it.stackTraceToString()}")
                    Toast.makeText(context, "Не удалось подключиться", Toast.LENGTH_SHORT).show()
                }
            ).storeIn(disposer)
    }

    private fun disconnect() {
        val socket = bluetoothSocket ?: return
        try {
            socket.close()
        } catch (ex: Exception) {
            Log.e("close socket", ex.stackTraceToString())
        }
        disconnectButton?.isVisible = false
        bluetoothSocket = null
    }

    private fun listen() {
        Single.fromCallable {
            val serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Walkie_Talkie_Service", secureUUID)
            try {
                serverSocket.accept()
            } catch (ex: Exception) {
                serverSocket?.close()
                throw Exception()
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    bluetoothSocket = it
                    disconnectButton?.isVisible = true
                },
                {
                    Log.e("listen", "${it.message}\n${it.stackTraceToString()}")
                    Toast.makeText(context, "Не удалось подключиться к прослушиванию", Toast.LENGTH_SHORT).show()
                }
            )
            .storeIn(disposer)
    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private val permissionsHelper = PermissionsHelper(permissions, PERMISSIONS_REQUEST_CODE) { requireActivity() }
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var searchButton: Button? = null
    private var disconnectButton: Button? = null

    private var deviceBundleSubject = PublishSubject.create<Bundle>()
    private val bluetoothDeviceSubject = deviceBundleSubject.map { extras ->
        extras.getParcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
    }.filter { device ->
        device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN && !device.name.isNullOrBlank()
    }.distinctUntilChanged()

    private val disposer = CompositeDisposable()

    private val devices = mutableSetOf<DeviceInfo>()
    private var adapter: DeviceListAdapter = DeviceListAdapter(itemClickHandler)

    private val searchReceiver = SimpleReceiver(action = BluetoothDevice.ACTION_FOUND) {
        deviceBundleSubject.onNext(it.extras!!)
    }
    private val searchStartReceiver = SimpleReceiver(
        action = BluetoothAdapter.ACTION_DISCOVERY_STARTED,
        isSingleEvent = true
    ) {
        searchButton?.isEnabled = false
        progress?.isVisible = true
        searchEndReceiver.register(requireContext())
    }
    private val searchEndReceiver = SimpleReceiver(
        action = BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
        isSingleEvent = true
    ) {
        searchButton?.isEnabled = true
        progress?.isVisible = false
        context?.unregisterReceiver(searchReceiver)
    }

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
        subscribeOnDevices()
        listen()
    }

    private fun startSearch() {
        permissionsHelper.withPermissions {
            stopDiscovery()
            searchStartReceiver.register(requireContext())
            searchReceiver.register(requireContext())
            bluetoothAdapter.startDiscovery()
        }
    }

    private fun subscribeOnDevices() {
        bluetoothDeviceSubject.subscribeOn(Schedulers.newThread())
            .map { device ->
                DeviceInfo(
                    address = device.address,
                    name = device.name
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { deviceInfo ->
                val devicesSize = devices.size
                devices.add(deviceInfo)
                if (devicesSize != devices.size) {
                    adapter.setDataList(devices.toList())
                }
            }.storeIn(disposer)
    }

    override fun onStart() {
        super.onStart()
        permissionsHelper.request()
    }

    override fun onStop() {
        super.onStop()
        stopDiscovery()
        disconnect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        disposer.clear()
    }

    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl()

    private fun stopDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        searchReceiver.unregister(requireContext())
        searchStartReceiver.unregister(requireContext())
        searchEndReceiver.unregister(requireContext())
        searchButton?.isEnabled = true
        progress?.isVisible = false
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
private const val SECURE_UUID = "fa87c0d0-afac-11de-8a39-0800200c9a66"