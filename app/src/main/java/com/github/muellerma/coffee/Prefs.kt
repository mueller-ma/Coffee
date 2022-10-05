package com.github.muellerma.coffee

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Prefs(context: Context) {
    var sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private set

    var timeout: Int
        get() = sharedPrefs.getString("timeout", "0")?.toInt() ?: 0
        set(value) = sharedPrefs.edit { putString("timeout", value.toString()) }

    var allowDimming: Boolean
        get() = sharedPrefs.getBoolean("allow_dimming", false)
        set(value) = sharedPrefs.edit { putBoolean("allow_dimming", value) }

    val useAlternateMode: Boolean
        get() = sharedPrefs.getBoolean("alternate_mode", false)

    var alternateModeOldTimeout: Int
        get() = sharedPrefs.getInt("alternate_mode_old_timeout", 0)
        set(value) = sharedPrefs.edit { putInt("alternate_mode_old_timeout", value) }
}