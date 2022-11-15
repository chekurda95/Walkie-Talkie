package com.chekurda.walkie_talkie.main_screen.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_fragment.BasePresenterFragment
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.domain.AudioStreamer
import com.chekurda.walkie_talkie.main_screen.domain.WifiDirectConnectionManager
import com.chekurda.walkie_talkie.main_screen.presentation.device_list.DeviceListAdapter
import com.chekurda.walkie_talkie.main_screen.presentation.views.RecordButtonView
import com.chekurda.walkie_talkie.main_screen.utils.PermissionsHelper
import com.chekurda.walkie_talkie.main_screen.utils.RecordingDeviceHelper

internal class MainScreenFragment : BasePresenterFragment<MainScreenContract.View, MainScreenContract.Presenter>(),
    MainScreenContract.View {

    companion object : MainScreenFragmentFactory {
        override fun createMainScreenFragment(): Fragment = MainScreenFragment()
    }

    override val layoutRes: Int = R.layout.main_screen_fragment
    private val adapter: DeviceListAdapter = DeviceListAdapter { presenter.onDeviceItemClicked(it) }

    private var recyclerView: RecyclerView? = null
    private var connectButton: Button? = null
    private var recordButton: RecordButtonView? = null

    private var permissionsHelper: PermissionsHelper? = null
    private var deviceHelper: RecordingDeviceHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionsHelper = PermissionsHelper(requireActivity(), permissions, PERMISSIONS_REQUEST_CODE)
        deviceHelper = RecordingDeviceHelper(requireActivity())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        /*recyclerView = view.findViewById<RecyclerView>(R.id.device_list).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MainScreenFragment.adapter
        }*/
        connectButton = view.findViewById<Button?>(R.id.connect_button).apply {
            setOnClickListener {
                if (true) {
                    presenter.onConnectClicked()
                } else {
                    presenter.onDisconnectClicked()
                }
            }
        }
        recordButton = view.findViewById<RecordButtonView>(R.id.record_button).apply {
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> presenter.onVoiceButtonStateChanged(isPressed = true)
                    MotionEvent.ACTION_CANCEL,
                    MotionEvent.ACTION_UP -> presenter.onVoiceButtonStateChanged(isPressed = false)
                }
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        deviceHelper?.configureDevice(isStartRecording = true)
    }

    override fun onStop() {
        super.onStop()
        deviceHelper?.configureDevice(isStartRecording = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        connectButton = null
        recordButton = null
    }

    override fun onDetach() {
        super.onDetach()
        deviceHelper = null
        permissionsHelper = null
    }

    override fun changeDeviceListVisibility(isVisible: Boolean) {
        TODO("Показать View списка девайсов.")
    }

    override fun updateDeviceList(deviceInfoList: List<DeviceInfo>) {
        TODO("Обновить список девайсов.")
    }

    override fun showConnectedState(connectedDevice: DeviceInfo) {
        TODO("Отобразить подключение к девайсу.")
    }

    override fun showConnectionError() {
        TODO("Соединение оборвалось, сбросить состояние и показать ошибку.")
    }

    override fun onDisconnected() {
        TODO("Рассоединение прошло успешно.")
    }

    override fun onInputAmplitudeChanged(amplitude: Float) {
        view?.post { TODO("Отобразить амплитуду входящего звука") }
    }

    override fun onOutputAmplitudeChanged(amplitude: Float) {
        view?.post { TODO("Отобразить амплитуду исходящего звука") }
    }

    override fun provideActivity(): Activity = requireActivity()

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
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
private const val PERMISSIONS_REQUEST_CODE = 102