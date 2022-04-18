package com.github.muellerma.coffee

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

fun Context.showToast(@StringRes msg: Int) {
    Toast
        .makeText(this, msg, Toast.LENGTH_SHORT)
        .show()
}

fun Context.prefs(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)