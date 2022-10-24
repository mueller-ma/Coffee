package com.github.muellerma.coffee

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors

class CoffeeApplication : Application() {
    var isRunning = false

    override fun onCreate() {
        super.onCreate()

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}