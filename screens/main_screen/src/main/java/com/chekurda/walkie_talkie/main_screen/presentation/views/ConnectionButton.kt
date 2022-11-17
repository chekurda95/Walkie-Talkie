package com.chekurda.walkie_talkie.main_screen.presentation.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.chekurda.walkie_talkie.main_screen.R

internal class ConnectionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatButton(context, attrs) {

    enum class ButtonState(val text: String, @DrawableRes val backgroundRes: Int) {
        CONNECT_SUGGESTION("Connect", R.drawable.connect_button_background),
        DISCONNECT_SUGGESTION("Disconnect", R.drawable.disconnect_button_background),
        WAITING_CONNECTION("Connecting...", R.drawable.connect_button_background)
    }

    var buttonState: ButtonState = ButtonState.CONNECT_SUGGESTION
        set(value) {
            field = value
            text = value.text
            background = ContextCompat.getDrawable(context, value.backgroundRes)
        }

    init {
        buttonState = ButtonState.CONNECT_SUGGESTION
    }
}