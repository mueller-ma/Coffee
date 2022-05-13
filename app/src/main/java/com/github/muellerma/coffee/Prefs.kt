package com.github.muellerma.coffee

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Prefs(context: Context) {
    private var prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val timeout: Int
        get() = prefs.getString("timeout", "0")?.toInt() ?: 0

    val allowDimming: Boolean
        get() = prefs.getBoolean("allow_dimming", false)

    val useAlternateMode: Boolean
        get() = prefs.getBoolean("alternate_mode", false)

    var alternateModeOldTimeout: Int
        get() = prefs.getInt("alternate_mode_old_timeout", 0)
        set(value) = prefs.edit { putInt("alternate_mode_old_timeout", value) }
}