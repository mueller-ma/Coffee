package com.github.muellerma.coffee

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

class ForegroundService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var screenStateListener = ScreenStateChangedReceiver()
    private var isScreenStateListenerRegistered = false

    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        if (intent?.action == STOP_ACTION) {
            Log.d(TAG, "Received stop action")
            changeState(this, STATE.STOP, false)
            return START_STICKY
        }

        @Suppress("DEPRECATION")
        wakeLock = getSystemService<PowerManager>()!!
            .newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                "Coffee::ForegroundService"
            )
        Log.d(TAG, "Acquire wakelock")
        wakeLock?.acquire()

        (application as CoffeeApplication).isRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CoffeeTile.requestTileStateUpdate(this)
        }
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

        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.action = STOP_ACTION
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, stopIntent, 0)
        } else {
            PendingIntent.getService(this, 0, stopIntent, 0)
        }

        val notification = getBaseNotification()
            .setContentText(getString(R.string.tap_to_turn_off))
            .setContentIntent(pendingStopIntent)
            .setPublicVersion(getBaseNotification().build())
            .build()

        (application as CoffeeApplication).isRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CoffeeTile.requestTileStateUpdate(this)
        }
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.keep_display_on))
            .setTicker(getString(R.string.keep_display_on))
            .setSmallIcon(R.drawable.ic_twotone_free_breakfast_24)
            .setOngoing(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        wakeLock?.release()
        if (isScreenStateListenerRegistered) {
            unregisterReceiver(screenStateListener)
            isScreenStateListenerRegistered = false
        }
        (application as CoffeeApplication).isRunning = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CoffeeTile.requestTileStateUpdate(this)
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

    companion object {
        private val TAG = ForegroundService::class.java.simpleName
        private const val STOP_ACTION = "stop_action"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "foreground_service"

        fun changeState(context: Context, newState: STATE, showToast: Boolean) {
            Log.d(TAG, "startOrStop($newState)")
            val start = when (newState) {
                STATE.START -> true
                STATE.STOP -> false
                STATE.TOGGLE -> !(context.applicationContext as CoffeeApplication).isRunning
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