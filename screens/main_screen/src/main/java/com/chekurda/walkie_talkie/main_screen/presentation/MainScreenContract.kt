package com.chekurda.walkie_talkie.main_screen.presentation

import android.app.Activity
import com.chekurda.common.base_fragment.BasePresenter
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.domain.AudioStreamer
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder.DeviceViewHolder

internal interface MainScreenContract {

    interface View : AudioStreamer.AmplitudeListener {
        fun changeDeviceListVisibility(isVisible: Boolean)
        fun updateDeviceList(deviceInfoList: List<DeviceInfo>)
        fun changeSearchState(isRunning: Boolean)
        fun showConnectionWaiting()
        fun showConnectedState(connectedDevice: DeviceInfo)
        fun showConnectionError()
        fun onDisconnected()
        fun provideActivity(): Activity
    }

    interface Presenter : BasePresenter<View>, DeviceViewHolder.ActionListener {
        fun onConnectClicked()
        fun onDisconnectClicked()
        fun onVoiceButtonStateChanged(isPressed: Boolean)
        fun onPermissionsGranted()
    }
}