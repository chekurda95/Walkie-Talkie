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
    private var connectedDevice: DeviceInfo? = null

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
        connectedDevice = null
    }

    override fun viewIsResumed() {
        super.viewIsResumed()
        wifiDirectManager.registerDirectListener(checkNotNull(view).provideActivity())
    }

    override fun viewIsPaused() {
        super.viewIsPaused()
        wifiDirectManager.unregisterDirectListener(checkNotNull(view).provideActivity())
    }

    override fun onConnectClicked() {
        wifiDirectManager.searchDevices()
        view?.changeDeviceListVisibility(isVisible = true)
    }

    override fun onDeviceItemClicked(deviceInfo: DeviceInfo) {
        connectedDevice = deviceInfo
        wifiDirectManager.connect(deviceInfo.address)
        view?.changeDeviceListVisibility(isVisible = false)
    }

    override fun onDisconnectClicked() {
        connectedDevice = null
        view?.onDisconnected()
    }

    override fun onVoiceButtonStateChanged(isPressed: Boolean) {
        wifiDirectManager.changeSteamDirection(isListening = !isPressed)
    }

    override fun onPeersChanged(devices: List<WifiP2pDevice>) {
        deviceInfoList = devices.map { it.deviceInfo }
        view?.updateDeviceList(deviceInfoList)
    }

    override fun onGroupConnectionStateChanged(isConnected: Boolean) {
        this.isConnected = isConnected
        if (!isConnected) {
            connectedDevice = null
            view?.onDisconnected()
        }
    }

    override fun onConnectionResult(isSuccess: Boolean) {
        val connectedDevice = connectedDevice
        if (isSuccess && connectedDevice != null) {
            view?.showConnectedState(connectedDevice)
        } else {
            this.connectedDevice = null
            view?.showConnectionError()
        }
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