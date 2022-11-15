package com.chekurda.walkie_talkie.main_screen.presentation

import android.app.Activity
import com.chekurda.common.base_fragment.BasePresenter
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo

internal interface MainScreenContract {

    interface View {
        fun changeDeviceListVisibility(isVisible: Boolean)
        fun updateDeviceList(deviceInfoList: List<DeviceInfo>)
        fun showConnectedState(connectedDevice: DeviceInfo)
        fun showConnectionError()
        fun onDisconnected()
        fun provideActivity(): Activity
    }

    interface Presenter : BasePresenter<View> {
        fun onConnectClicked()
        fun onDeviceItemClicked(deviceInfo: DeviceInfo)
        fun onDisconnectClicked()
        fun onVoiceButtonStateChanged(isPressed: Boolean)
    }
}