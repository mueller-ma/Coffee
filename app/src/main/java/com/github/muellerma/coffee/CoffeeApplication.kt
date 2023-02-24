package com.github.muellerma.coffee

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors

class CoffeeApplication : Application() {
    var observers = mutableListOf<ServiceStatusObserver>()
    var lastStatusUpdate: ServiceStatus = ServiceStatus.Stopped
        private set

    override fun onCreate() {
        super.onCreate()

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    fun notifyObservers(status: ServiceStatus) {
        lastStatusUpdate = status
        observers.forEach { observer ->
            observer.onServiceStatusUpdate(status)
        }
    }
}

interface ServiceStatusObserver {
    fun onServiceStatusUpdate(status: ServiceStatus)
}

sealed class ServiceStatus {
    class Running(val remainingSeconds: Long?) : ServiceStatus() {
        override fun toString() = "ServiceStatus.Running($remainingSeconds)"
    }
    object Stopped : ServiceStatus()
}