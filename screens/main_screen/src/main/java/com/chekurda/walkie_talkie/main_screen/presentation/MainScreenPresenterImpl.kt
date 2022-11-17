package com.chekurda.walkie_talkie.main_screen.presentation

import android.net.wifi.p2p.WifiP2pDevice
import com.chekurda.common.base_fragment.BasePresenterImpl
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
    private var hasPermissions: Boolean = false
    private var isListening: Boolean = true

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
        view?.changeDeviceListVisibility(isVisible = true)
    }

    override fun onDeviceItemClicked(deviceInfo: DeviceInfo) {
        wifiDirectManager.connect(deviceInfo.address)
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
        if (devices.isEmpty() && deviceInfoList.isNotEmpty()) return
        deviceInfoList = devices.map { it.deviceInfo }
        view?.updateDeviceList(deviceInfoList)
    }

    override fun onSearchStateChanged(isRunning: Boolean) {
        view?.changeSearchState(isRunning)
    }

    override fun onConnectionSuccess(device: WifiP2pDevice) {
        this.isConnected = true
        view?.changeDeviceListVisibility(isVisible = false)
        view?.showConnectedState(device.deviceInfo)
        wifiDirectManager.stopSearchDevices()
    }

    override fun onConnectionCanceled(isError: Boolean) {
        if (isError) {
            view?.showConnectionError()
        } else {
            view?.onDisconnected()
        }
        wifiDirectManager.startSearchDevices()
    }

    override fun onInputAmplitudeChanged(amplitude: Float) {
        view?.onInputAmplitudeChanged(amplitude)
    }

    override fun onOutputAmplitudeChanged(amplitude: Float) {
        view?.onOutputAmplitudeChanged(amplitude)
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.release()
    }
}