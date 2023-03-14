package com.github.muellerma.coffee

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ForegroundService : Service(), ServiceStatusObserver {
    private var wakeLock: PowerManager.WakeLock? = null
    private var timeoutJob: Job? = null
    private var screenStateListener = ScreenStateChangedReceiver()
    private var prefsChangeListener = PrefChangeListener()
    private var isScreenStateListenerRegistered = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        val prefs = Prefs(applicationContext)
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(TAG, "Received stop action")
                changeState(this, STATE.STOP, false)
                return START_STICKY
            }
            ACTION_CHANGE_PREF_TIMEOUT -> {
                Log.d(TAG, "Change timeout")
                prefs.timeout = prefs.nextTimeout
            }
            ACTION_CHANGE_PREF_ALLOW_DIMMING -> {
                Log.d(TAG, "Change allow dimming")
                prefs.allowDimming = !prefs.allowDimming
            }
        }

        coffeeApp().observers.add(this@ForegroundService)

        startWakeLockOrAlternateMode()
        startTimeoutJob()

        if (!isScreenStateListenerRegistered) {
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            registerReceiver(screenStateListener, intentFilter)
            isScreenStateListenerRegistered = true
        }
        return START_STICKY
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService<NotificationManager>()!!
            with(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_MIN
                )
            ) {
                setShowBadge(true)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                nm.createNotificationChannel(this)
            }
        }

        val prefs = Prefs(this)
        prefs.sharedPrefs.registerOnSharedPreferenceChangeListener(prefsChangeListener)

        startForeground(NOTIFICATION_ID, getBaseNotification(getString(R.string.app_name)).build())
    }

    @SuppressLint("WakelockTimeout")
    private fun startWakeLockOrAlternateMode() {
        val prefs = Prefs(this)

        if (prefs.useAlternateMode) {
            prefs.alternateModeOldTimeout = contentResolver.getSystemScreenTimeout()
            val success = contentResolver.setSystemScreenTimeout(Int.MAX_VALUE)
            if (!success) {
                openSystemScreenTimeoutPermissions()
                changeState(this, STATE.STOP, false)
            }
        } else {
            @Suppress("DEPRECATION")
            val wakeLockLevel = if (prefs.allowDimming) {
                PowerManager.SCREEN_DIM_WAKE_LOCK
            } else {
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK
            }
            wakeLock.safeRelease()
            wakeLock = getSystemService<PowerManager>()!!
                .newWakeLock(
                    wakeLockLevel or PowerManager.ON_AFTER_RELEASE,
                    "Coffee::ForegroundService"
                )
            Log.d(TAG, "Acquire wakelock with level $wakeLockLevel")
            wakeLock?.acquire()
        }
    }

    private fun stopWakeLockOrAlternateMode() {
        wakeLock.safeRelease()
        val prefs = Prefs(this)
        if (prefs.useAlternateMode) {
            val success = contentResolver.setSystemScreenTimeout(prefs.alternateModeOldTimeout)
            if (!success) {
                showToast(R.string.alternate_mode_unable_to_set_old_timeout)
            }
        }
    }

    private fun startTimeoutJob() {
        timeoutJob?.cancel(CancellationException("startTimeoutJob() called"))
        val prefs = Prefs(this)
        val timeout = prefs.timeout
        if (timeout == 0) {
            Log.d(TAG, "No timeout set")
            coffeeApp().notifyObservers(ServiceStatus.Running(null))
            return
        }
        timeoutJob = CoroutineScope(Dispatchers.Main + Job()).launch {
            Log.d(TAG, "Schedule timeout for $timeout minutes")
            (timeout.minutes.inWholeSeconds downTo 0).forEach { remainingSeconds ->
                Log.d(TAG, "Remaining seconds = $remainingSeconds")
                coffeeApp().notifyObservers(
                    ServiceStatus.Running(remainingSeconds)
                )
                delay(1.seconds)
            }
            Log.d(TAG, "Timeout reached, stop coffee")
            changeState(this@ForegroundService, STATE.STOP, true)
        }
    }

    private fun getBaseNotification(title: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_twotone_free_breakfast_24)
            .setOngoing(true)
            .setShowWhen(false)
            .setColor(ContextCompat.getColor(applicationContext, R.color.coffee_brown))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
    }

    private fun getRunningNotification(prefs: Prefs): Notification? {
        val stopIntent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_STOP
        }

        val title = when (val status = coffeeApp().lastStatusUpdate) {
            is ServiceStatus.Stopped -> return null
            is ServiceStatus.Running -> {
                if (status.remainingSeconds == null) {
                    getString(R.string.notification_title_no_timeout)
                } else {
                    getString(R.string.notification_title_timeout, status.remainingSeconds.toFormattedTime())
                }
            }
        }

        return getBaseNotification(title)
            .setContentText(getString(R.string.tap_to_turn_off))
            .setContentIntent(getPendingIntentForService(stopIntent, PendingIntent_Immutable, 0))
            .addAction(getTimeoutAction())
            .addAction(getDimmingAction(prefs))
            .setPublicVersion(getBaseNotification(title).build())
            .build()
    }

    private fun getTimeoutAction(): NotificationCompat.Action {
        Log.d(TAG, "getTimeoutAction()")
        val intent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_CHANGE_PREF_TIMEOUT
        }
        return NotificationCompat.Action(
            R.drawable.ic_baseline_access_time_24,
            getString(R.string.timeout_next),
            getPendingIntentForService(intent, PendingIntent_Immutable or PendingIntent.FLAG_UPDATE_CURRENT, 1)
        )
    }

    private fun getDimmingAction(prefs: Prefs): NotificationCompat.Action {
        val intent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_CHANGE_PREF_ALLOW_DIMMING
        }
        val title = if (prefs.allowDimming) R.string.allow_dimming_disable else R.string.allow_dimming_enable
        return NotificationCompat.Action(
            R.drawable.ic_baseline_brightness_medium_24,
            getString(title),
            getPendingIntentForService(intent, PendingIntent_Immutable or PendingIntent.FLAG_UPDATE_CURRENT, 2)
        )
    }

    private fun getPendingIntentForService(intent: Intent, flags: Int, requestCode: Int): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, requestCode, intent, flags)
        } else {
            PendingIntent.getService(this, requestCode, intent, flags)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()

        stopWakeLockOrAlternateMode()
        timeoutJob?.cancel(CancellationException("Coffee was stopped"))
        Prefs(applicationContext)
            .sharedPrefs.
            unregisterOnSharedPreferenceChangeListener(prefsChangeListener)
        if (isScreenStateListenerRegistered) {
            unregisterReceiver(screenStateListener)
            isScreenStateListenerRegistered = false
        }
        coffeeApp().apply {
            observers.remove(this@ForegroundService)
            notifyObservers(ServiceStatus.Stopped)
        }
    }

    override fun onServiceStatusUpdate(status: ServiceStatus) {
        val nm = getSystemService<NotificationManager>()!!
        val notification = getRunningNotification(Prefs(applicationContext))
        if (notification == null) {
            nm.cancel(NOTIFICATION_ID)
        } else {
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private class ScreenStateChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_SCREEN_OFF || context == null) {
                return
            }
            Log.d(TAG, "Received screen off event: Stop service")
            changeState(context, STATE.STOP, false)
        }
    }

    private inner class PrefChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            Log.d(TAG, "Pref '$key' has been changed")

            stopWakeLockOrAlternateMode()
            startWakeLockOrAlternateMode()
            startTimeoutJob()
        }

    }

    companion object {
        private val TAG = ForegroundService::class.java.simpleName
        private const val ACTION_STOP = "stop_action"
        const val ACTION_CHANGE_PREF_TIMEOUT = "change_pref_timeout"
        private const val ACTION_CHANGE_PREF_ALLOW_DIMMING = "change_pref_dimming"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "foreground_service"

        fun changeState(context: Context, newState: STATE, showToast: Boolean) {
            Log.d(TAG, "changeState($newState)")
            val start = when (newState) {
                STATE.START -> true
                STATE.STOP -> false
                STATE.TOGGLE -> {
                    val app = context.applicationContext as CoffeeApplication
                    app.lastStatusUpdate is ServiceStatus.Stopped
                }
            }
            val intent = Intent(context, ForegroundService::class.java)
            val message = if (start) {
                ContextCompat.startForegroundService(context, intent)
                R.string.turned_on
            } else {
                context.stopService(intent)
                R.string.turned_off
            }

            if (showToast) {
                context.showToast(message)
            }
        }

        enum class STATE {
            START,
            STOP,
            TOGGLE
        }
    }
}
