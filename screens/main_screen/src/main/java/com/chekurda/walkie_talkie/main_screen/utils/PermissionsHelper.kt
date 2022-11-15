package com.chekurda.walkie_talkie.main_screen.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Вспомогательный класс для работы с разрешениями.
 *
 * @property permissions Разрешения, которые необходимо проверять и запрашивать.
 * @property requestCode Код запроса разрешений.
 */
internal class PermissionsHelper(
    private val activity: Activity,
    private val permissions: Array<String>,
    private val requestCode: Int
) {

    /**
     * Запрос разрешений [permissions].
     */
    fun request() {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * Выполнить действие [action], если имеются указанные разрешения [permissions],
     * если разрешений нет, то произойдет запрос по коду [requestCode].
     */
    fun withPermissions(action: () -> Unit) =
        withPermissions(permissions, requestCode, action)

    private fun withPermissions(
        permissions: Array<String>,
        requestCode: Int,
        action: () -> Unit
    ) {
        if (checkPermissions(permissions)) {
            action()
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        permissions.forEach {
            val hasPermission = ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return false
        }
        return true
    }
}