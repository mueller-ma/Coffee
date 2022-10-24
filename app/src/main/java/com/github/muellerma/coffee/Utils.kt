package com.github.muellerma.coffee

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat

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

fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions
        .map { permission -> ActivityCompat.checkSelfPermission(this, permission) }
        .all { result -> result == PackageManager.PERMISSION_GRANTED }
}

fun String.openInBrowser(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG, "Unable to open url in browser: $intent")
        context.showToast(R.string.error_no_browser_found)
    }
}

val PendingIntent_Immutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    PendingIntent.FLAG_IMMUTABLE
} else {
    0
}

val PendingIntent_Mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_MUTABLE
} else {
    0
}

fun PowerManager.WakeLock?.safeRelease() {
    try {
        this?.release()
    } catch (e: RuntimeException) {
        Log.d(TAG, "Couldn't release wakelock $this", e)
    }
}