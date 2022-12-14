package com.chekurda.walkie_talkie.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chekurda.walkie_talkie.AppPlugin
import com.chekurda.walkie_talkie.R

/**
 * Activity главного экрана.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, AppPlugin.mainScreenFragmentFactory.createMainScreenFragment())
                .commit()
        }
    }
}
