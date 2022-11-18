package com.chekurda.walkie_talkie.main_screen.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_fragment.BasePresenterFragment
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.domain.AudioStreamer
import com.chekurda.walkie_talkie.main_screen.domain.WifiDirectConnectionManager
import com.chekurda.walkie_talkie.main_screen.presentation.views.ConnectionButton
import com.chekurda.walkie_talkie.main_screen.presentation.views.ConnectionButton.*
import com.chekurda.walkie_talkie.main_screen.presentation.views.ConnectionInfoView
import com.chekurda.walkie_talkie.main_screen.presentation.views.ConnectionInfoView.ConnectionInfo
import com.chekurda.walkie_talkie.main_screen.presentation.views.RecordButtonView
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.DevicePickerView
import com.chekurda.walkie_talkie.main_screen.utils.PermissionsHelper
import com.chekurda.walkie_talkie.main_screen.utils.RecordingDeviceHelper

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    override val layoutRes: Int = R.layout.main_screen_fragment

    private var connectionButton: ConnectionButton? = null
    private var recordButton: RecordButtonView? = null
    private var devicePicker: DevicePickerView? = null
    private var connectionInfoView: ConnectionInfoView? = null

    private var permissionsHelper: PermissionsHelper? = null
    private var deviceHelper: RecordingDeviceHelper? = null
    private var wifiManager: WifiManager? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionsHelper = PermissionsHelper(requireActivity(), permissions, PERMISSIONS_REQUEST_CODE)
        deviceHelper = RecordingDeviceHelper(requireActivity())
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        addBackStackCallback()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        devicePicker = view.findViewById<DevicePickerView>(R.id.device_picker).apply {
            itemActionListener = presenter
            setOnClickListener { devicePicker?.hide() }
        }
        connectionButton = view.findViewById<ConnectionButton>(R.id.connect_button).apply {
            setOnClickListener {
                when (connectionButton!!.buttonState) {
                    ButtonState.CONNECT_SUGGESTION -> onConnectButtonClicked()
                    ButtonState.DISCONNECT_SUGGESTION,
                    ButtonState.WAITING_CONNECTION -> presenter.onDisconnectClicked()
                }
            }
        }
        recordButton = view.findViewById<RecordButtonView>(R.id.record_button).apply {
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.isPressed = true
                        presenter.onVoiceButtonStateChanged(isPressed = true)
                    }
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> {
                        view.isPressed = false
                        recordButton?.animateAmplitudeCancel()
                        presenter.onVoiceButtonStateChanged(isPressed = false)
                    }
                }
                true
            }
        }
        connectionInfoView = view.findViewById(R.id.connection_info_view)
    }

    override fun onStart() {
        super.onStart()
        deviceHelper?.configureDevice(isStartRecording = true)
        checkNotNull(permissionsHelper).withPermissions {
            presenter.onPermissionsGranted()
        }
    }

    override fun onStop() {
        super.onStop()
        deviceHelper?.configureDevice(isStartRecording = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connectionButton = null
        recordButton = null
        devicePicker = null
        connectionInfoView = null
    }

    override fun onDetach() {
        super.onDetach()
        deviceHelper = null
        permissionsHelper = null
        wifiManager = null
    }

    override fun changeDeviceListVisibility(isVisible: Boolean) {
        devicePicker?.apply {
            if (isVisible) show()
            else hide()
        }
    }

    override fun updateDeviceList(deviceInfoList: List<DeviceInfo>) {
        devicePicker?.updateDeviceList(deviceInfoList)
    }

    override fun changeSearchState(isRunning: Boolean) {
        devicePicker?.updateSearchState(isRunning)
    }

    override fun showConnectionWaiting() {
        connectionButton?.buttonState = ButtonState.WAITING_CONNECTION
    }

    override fun showConnectedState(connectedDevice: DeviceInfo) {
        connectionButton?.buttonState = ButtonState.DISCONNECT_SUGGESTION
        recordButton?.isEnabled = true
        connectionInfoView?.connectionData = ConnectionInfo(
            deviceName = connectedDevice.name,
            isConnected = true
        )
    }

    override fun showError(errorMessage: Int) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        clearState()
    }

    override fun onDisconnected() {
        clearState()
    }

    private fun clearState() {
        connectionButton?.buttonState = ButtonState.CONNECT_SUGGESTION
        recordButton?.isEnabled = false
        connectionInfoView?.connectionData = ConnectionInfo()
    }

    override fun onInputAmplitudeChanged(amplitude: Float) {
        view?.post { connectionInfoView?.inputAmplitude = amplitude }
    }

    override fun onOutputAmplitudeChanged(amplitude: Float) {
        view?.post {
            if (recordButton?.isPressed == false) return@post
            recordButton?.amplitude = amplitude
        }
    }

    private fun onConnectButtonClicked() {
        permissionsHelper?.withPermissions {
            if (wifiManager?.isWifiEnabled == true) {
                presenter.onConnectClicked()
            } else {
                requireContext().startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                Toast.makeText(context, "Walkie-talkie require working Wi-Fi", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun provideActivity(): Activity = requireActivity()

    private fun addBackStackCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (devicePicker?.hide() == false) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }

    /**
     * DI Press F.
     */
    override fun createPresenter(): MainScreenContract.Presenter = MainScreenPresenterImpl(
        WifiDirectConnectionManager(
            AudioStreamer()
        )
    )
}

private val permissions = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102