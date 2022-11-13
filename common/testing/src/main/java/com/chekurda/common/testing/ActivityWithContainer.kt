package ru.tensor.sbis.common.testing

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout

/**
 * [AppCompatActivity] с контейнером, имеющим id == 1,
 */
open class ActivityWithContainer : AppCompatActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FrameLayout(this).also {
            it.id = 1
            setContentView(it)
        }
    }
}