package com.chekurda.walkie_talkie.main_screen.presentation

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.annotation.StringRes
import com.chekurda.common.base_fragment.BasePresenterImpl
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.data.deviceInfo
import com.chekurda.walkie_talkie.main_screen.domain.AudioStreamer
import com.chekurda.walkie_talkie.main_screen.domain.WifiDirectConnectionManager

internal class MainScreenPresenterImpl(
    private val wifiDirectManager: WifiDirectConnectionManager
) : BasePresenterImpl<MainScreenContract.View>(),
    MainScreenContract.Presenter,
    WifiDirectConnectionManager.ProcessListener,
    AudioStreamer.AmplitudeListener {

    private var deviceInfoList: List<DeviceInfo> = emptyList()
    private var isConnected: Boolean = false
    private var isWaitingConnection: Boolean = false
    private var hasPermissions: Boolean = false

    init {
        wifiDirectManager.apply {
            processListener = this@MainScreenPresenterImpl
            amplitudeListener = this@MainScreenPresenterImpl
        }
    }

    override fun attachView(view: MainScreenContract.View) {
        super.attachView(view)
        wifiDirectManager.init(view.provideActivity())
    }

    override fun detachView() {
        super.detachView()
        wifiDirectManager.clear()
    }

    override fun viewIsResumed() {
        super.viewIsResumed()
        if (hasPermissions) {
            wifiDirectManager.registerDirectListener(checkNotNull(view).provideActivity())
            if (!isConnected) wifiDirectManager.startSearchDevices()
        }
    }

    override fun viewIsPaused() {
        super.viewIsPaused()
        if (hasPermissions) {
            if (!isConnected) wifiDirectManager.stopSearchDevices()
            wifiDirectManager.unregisterDirectListener(checkNotNull(view).provideActivity())
        }
    }

    override fun onConnectClicked() {
        if (!hasPermissions) {
            wifiDirectManager.registerDirectListener(view!!.provideActivity())
            wifiDirectManager.startSearchDevices()
            hasPermissions = true
        }
        view?.changeDeviceListVisibility(isVisible = true)
    }

    override fun onDeviceItemClicked(deviceInfo: DeviceInfo) {
        isWaitingConnection = true
        wifiDirectManager.connect(
            WifiP2pDevice().apply {
                deviceAddress = deviceInfo.address
                deviceName = deviceInfo.name
            }
        )
        view?.changeDeviceListVisibility(isVisible = false)
    }

    override fun onWaitingConnection() {
        if (!hasPermissions) return
        view?.showConnectionWaiting()
    }

    override fun onDisconnectClicked() {
        if (!hasPermissions) return
        wifiDirectManager.disconnect()
    }

    override fun onPermissionsGranted() {
        hasPermissions = true
        if (!isConnected) wifiDirectManager.startSearchDevices()
    }

    override fun onVoiceButtonStateChanged(isPressed: Boolean) {
        wifiDirectManager.changeSteamDirection(isListening = !isPressed)
    }

    override fun onPeersChanged(devices: List<WifiP2pDevice>) {
        deviceInfoList = devices.map { it.deviceInfo }
        view?.updateDeviceList(deviceInfoList)
    }

    override fun onSearchStateChanged(isRunning: Boolean) {
        view?.changeSearchState(isRunning)
        if (hasPermissions && !isRunning && !isConnected) {
            wifiDirectManager.startSearchDevices()
        }
        Log.d("onSearchStateChanged", "onSearchStateChanged $isRunning")
    }

    override fun onConnectionSuccess(device: WifiP2pDevice) {
        isConnected = true
        isWaitingConnection = false
        view?.changeDeviceListVisibility(isVisible = false)
        view?.showConnectedState(device.deviceInfo)
        wifiDirectManager.stopSearchDevices()
    }

    override fun onConnectionCanceled(isError: Boolean) {
        if (isError) {
            view?.showError(getErrorMessage())
        } else {
            view?.onDisconnected()
        }
        isConnected = false
        isWaitingConnection = false
        wifiDirectManager.startSearchDevices()
    }

    override fun onInputAmplitudeChanged(amplitude: Float) {
        view?.onInputAmplitudeChanged(amplitude)
    }

    override fun onOutputAmplitudeChanged(amplitude: Float) {
        view?.onOutputAmplitudeChanged(amplitude)
    }

    @StringRes
    private fun getErrorMessage(): Int = when {
        isConnected -> R.string.disconnect_error
        !hasPermissions -> R.string.permissions_error
        isWaitingConnection -> R.string.try_connection_error
        else -> R.string.other_error_message
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.release()
    }
}