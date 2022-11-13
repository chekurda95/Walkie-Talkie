package com.chekurda.main_screen.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_fragment.BasePresenterFragment
import com.chekurda.common.storeIn
import com.chekurda.main_screen.R
import com.chekurda.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.main_screen.presentation.device_list.DeviceListAdapter
import com.chekurda.main_screen.presentation.device_list.DeviceView
import com.chekurda.main_screen.presentation.device_list.DeviceViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.SerialDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    private val itemClickHandler = DeviceViewHolder.ActionListener {

    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var recyclerView: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var searchButton: Button? = null

    private var deviceBundleSubject = PublishSubject.create<Bundle>()
    private val bluetoothDeviceSubject = deviceBundleSubject.map { extras ->
        extras.getParcelable<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
    }.filter { it.type == BluetoothDevice.DEVICE_TYPE_CLASSIC && !it.name.isNullOrBlank() }
        .distinctUntilChanged()
    private val disposer = SerialDisposable()
    private val devices = mutableSetOf<DeviceView.DeviceData>()
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
        progress = view.findViewById(R.id.search_progress)
        searchButton = view.findViewById<Button?>(R.id.search_button).apply {
            setOnClickListener { startSearch() }
        }
        subscribeOnDevices()
    }

    private fun startSearch() {
        stopDiscovery()
        searchStartReceiver.register(requireContext())
        searchReceiver.register(requireContext())
        bluetoothAdapter.startDiscovery()
    }

    private fun subscribeOnDevices() {
        bluetoothDeviceSubject.subscribeOn(Schedulers.newThread())
            .map { device -> DeviceView.DeviceData(device.address, device.name) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { deviceData ->
                val devicesSize = devices.size
                devices.add(deviceData)
                if (devicesSize != devices.size) {
                    adapter.setDataList(devices.toList())
                }
            }.storeIn(disposer)
    }

    override fun onStart() {
        super.onStart()
        ActivityCompat.requestPermissions(requireActivity(), permissions, 200)
    }

    override fun onStop() {
        super.onStop()
        stopDiscovery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        disposer.dispose()
    }

    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl()

    private fun stopDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        searchReceiver.unregister(requireContext())
        searchStartReceiver.unregister(requireContext())
        searchEndReceiver.unregister(requireContext())
    }
}

private class SimpleReceiver(
    action: String,
    private val isSingleEvent: Boolean = false,
    private val onReceive: (Intent) -> Unit
) : BroadcastReceiver() {

    private val intentFilter = IntentFilter(action)
    private var isRegistered = false

    override fun onReceive(context: Context, intent: Intent) {
        onReceive(intent)
        if (isSingleEvent) unregister(context)
    }

    fun register(context: Context) {
        if (isRegistered) return
        context.registerReceiver(this, intentFilter)
        isRegistered = true
    }

    fun unregister(context: Context) {
        if (!isRegistered) return
        context.unregisterReceiver(this)
        isRegistered = false
    }
}

private val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)