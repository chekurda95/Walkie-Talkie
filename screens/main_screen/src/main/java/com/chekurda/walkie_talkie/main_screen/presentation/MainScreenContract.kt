package com.chekurda.walkie_talkie.main_screen.presentation

import android.app.Activity
import androidx.annotation.StringRes
import com.chekurda.common.base_fragment.BasePresenter
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.domain.AudioStreamer
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder.DeviceViewHolder

/**
 * Контракт главного экрана.
 */
internal interface MainScreenContract {

    /**
     * View контракт главного экрана.
     */
    interface View : AudioStreamer.AmplitudeListener {

        /**
         * Изменить видимость пикера девайсов для подключения.
         */
        fun changeDevicePickerVisibility(isVisible: Boolean)

        /**
         * Обновить список девайсов.
         */
        fun updateDeviceList(deviceInfoList: List<DeviceInfo>)

        /**
         * Изменить состояние поиска девайсов.
         */
        fun changeSearchState(isRunning: Boolean)

        /**
         * Показать ожидание подключения.
         */
        fun showConnectionWaiting()

        /**
         * Отобразить состояние подключения.
         */
        fun showConnectedState(connectedDevice: DeviceInfo)

        /**
         * Показать ошибку.
         *
         * @param errorMessage ресурс текста ошибки.
         */
        fun showError(@StringRes errorMessage: Int)

        /**
         * Отобразить состояние дисконекта.
         */
        fun onDisconnected()

        /**
         * Предоставить Activity.
         */
        fun provideActivity(): Activity
    }

    /**
     * Контракт презентера главного экрана.
     */
    interface Presenter : BasePresenter<View>, DeviceViewHolder.ActionListener {

        /**
         * Произошел клик по кнопке подключения.
         */
        fun onConnectClicked()

        /**
         * Произошел клик по кнопке отключения.
         */
        fun onDisconnectClicked()

        /**
         * Изменилось состояние кнопки записи.
         */
        fun onRecordButtonStateChanged(isPressed: Boolean)

        /**
         * Получены все необходимые разрешения.
         */
        fun onPermissionsGranted()
    }
}