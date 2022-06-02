package com.github.muellerma.coffee

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Prefs(context: Context) {
    var sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private set

    val timeout: Int
        get() = sharedPrefs.getString("timeout", "0")?.toInt() ?: 0

    val allowDimming: Boolean
        get() = sharedPrefs.getBoolean("allow_dimming", false)

    val useAlternateMode: Boolean
        get() = sharedPrefs.getBoolean("alternate_mode", false)

    var alternateModeOldTimeout: Int
        get() = sharedPrefs.getInt("alternate_mode_old_timeout", 0)
        set(value) = sharedPrefs.edit { putInt("alternate_mode_old_timeout", value) }
}