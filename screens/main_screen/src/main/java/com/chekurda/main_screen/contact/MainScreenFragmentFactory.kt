package com.chekurda.main_screen.contact

import androidx.fragment.app.Fragment
import com.chekurda.common.plugin_struct.Feature

interface MainScreenFragmentFactory : Feature {

    fun createMainScreenFragment(): Fragment
}