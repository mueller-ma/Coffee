package com.github.muellerma.coffee

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.annotation.StringRes

private const val TAG = "Utils"

fun Context.showToast(@StringRes msg: Int) {
    Toast
        .makeText(this, msg, Toast.LENGTH_SHORT)
        .show()
}

fun ContentResolver.getSystemScreenTimeout(): Int {
    return Settings.System.getInt(this, Settings.System.SCREEN_OFF_TIMEOUT)
}

@CheckResult
fun ContentResolver.setSystemScreenTimeout(timeout: Int): Boolean {
    return try {
        Settings.System.putInt(this, Settings.System.SCREEN_OFF_TIMEOUT, timeout)
        true
    } catch (e: SecurityException) {
        Log.e(TAG, "Error writing screen timeout", e)
        false
    }
}

fun Context.openSystemScreenTimeoutPermissions() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return
    }

    showToast(R.string.alternate_mode_permissions)
    startActivity(
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            .setData(Uri.parse("package:$packageName"))
    )
}