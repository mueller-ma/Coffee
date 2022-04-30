package com.github.muellerma.coffee

import android.content.Context
import androidx.preference.PreferenceManager

class Prefs(context: Context) {
    private var prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val timeout: Int
        get() { return prefs.getString("timeout", "0")?.toInt() ?: 0 }
}