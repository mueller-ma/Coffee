package com.github.muellerma.coffee

import android.app.Application
import androidx.preference.PreferenceManager

class CoffeeApplication : Application() {
    var isRunning = false

    override fun onCreate() {
        super.onCreate()

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)
    }
}