package com.github.muellerma.coffee

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.time.Duration

// Android 16 API level constant (Baklava)
private const val ANDROID_16_BAKLAVA = 36

/**
 * Manages Android 16+ Progress-centric notifications for Coffee app.
 * This creates live activity-style notifications showing wake-lock status and timer progress.
 */
@RequiresApi(ANDROID_16_BAKLAVA)
class ProgressNotificationManager(private val context: Context) {

    /**
     * Creates an Android 16 progress-centric notification showing Coffee's wake-lock status
     */
    fun createProgressNotification(
        remaining: Duration?,
        timeout: Int,
        prefs: Prefs
    ): Notification {
        Log.d(TAG, "Creating Android 16 progress-centric notification")

        val stopIntent = Intent(context, ForegroundService::class.java).apply {
            action = "stop_action"
        }

        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Build the progress style notification
        val progressStyle = NotificationCompat.ProgressStyle()
        
        // Define the journey for the wake-lock session
        when {
            remaining == null -> {
                // No timeout - indeterminate progress
                progressStyle.setProgressIndeterminate(true)
                // Add a single segment for visual styling
                progressStyle.setProgressSegments(
                    listOf(
                        NotificationCompat.ProgressStyle.Segment(100)
                            .setColor(ContextCompat.getColor(context, R.color.coffee_brown))
                    )
                )
            }
            else -> {
                // With timeout - show progress through the timer
                val totalSeconds = timeout * 60
                val elapsedSeconds = (totalSeconds - remaining.inWholeSeconds.toInt()).coerceAtLeast(0)
                
                // Add progress segment
                progressStyle.setProgressSegments(
                    listOf(
                        NotificationCompat.ProgressStyle.Segment(totalSeconds)
                            .setColor(ContextCompat.getColor(context, R.color.coffee_brown))
                    )
                )
                
                // Set current progress
                progressStyle.setProgress(elapsedSeconds)
            }
        }

        // Determine notification title based on remaining time
        val title = if (remaining == null) {
            context.getString(R.string.notification_title_no_timeout)
        } else {
            context.getString(R.string.notification_title_timeout, remaining.toFormattedTime())
        }

        // Status chip configuration - shows coffee icon and time remaining
        val whenTime = if (remaining != null) {
            // Set when time to the end of the countdown (now + remaining time)
            System.currentTimeMillis() + remaining.inWholeMilliseconds
        } else {
            // For no-timeout mode, don't show time in status chip
            0L
        }

        // Short critical text for status chip (7 characters max suggested)
        val chipText = if (remaining != null && remaining.inWholeMinutes > 0) {
            "${remaining.inWholeMinutes}min"  // Shows remaining minutes in status chip
        } else {
            null  // No text for indeterminate mode or when < 1 minute
        }

        // Build the notification with progress style
        val builder = NotificationCompat.Builder(context, ForegroundService.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_free_breakfast_24_accent)  // Coffee icon appears in status chip
            .setContentTitle(title)
            .setContentText(context.getString(R.string.tap_to_turn_off))
            .setOngoing(true)
            .setRequestPromotedOngoing(true)  // Request promotion for Live Update - required for status chip
            .setShowWhen(remaining != null)  // Only show time if we have a timeout
            .setWhen(whenTime)  // Countdown end time for status chip
            .setUsesChronometer(remaining != null)  // Use chronometer for countdown timer
            .setChronometerCountDown(remaining != null)  // Count down to zero
            .setShortCriticalText(chipText)  // Shows in status chip (e.g., "5min")
            .setColor(ContextCompat.getColor(context, R.color.coffee_brown))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(stopPendingIntent)
            .setStyle(progressStyle)

        // Add action buttons
        addActionButtons(builder, prefs)

        return builder.build()
    }

    private fun addActionButtons(builder: NotificationCompat.Builder, prefs: Prefs) {
        // Add timeout change action
        val timeoutIntent = Intent(context, ForegroundService::class.java).apply {
            action = ForegroundService.ACTION_CHANGE_PREF_TIMEOUT
        }
        val timeoutPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                1,
                timeoutIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                context,
                1,
                timeoutIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        
        val timeoutAction = NotificationCompat.Action(
            R.drawable.ic_baseline_access_time_24,
            context.getString(R.string.timeout_next),
            timeoutPendingIntent
        )

        // Add dimming toggle action
        val dimmingIntent = Intent(context, ForegroundService::class.java).apply {
            action = ForegroundService.ACTION_CHANGE_PREF_ALLOW_DIMMING
        }
        val dimmingPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                2,
                dimmingIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                context,
                2,
                dimmingIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        
        val dimmingTitle = if (prefs.allowDimming) {
            R.string.allow_dimming_disable
        } else {
            R.string.allow_dimming_enable
        }
        
        val dimmingAction = NotificationCompat.Action(
            R.drawable.ic_baseline_brightness_medium_24,
            context.getString(dimmingTitle),
            dimmingPendingIntent
        )

        builder.addAction(timeoutAction)
        builder.addAction(dimmingAction)
    }

    companion object {
        private val TAG = ProgressNotificationManager::class.java.simpleName
        
        /**
         * Check if progress-centric notifications are available (Android 16+)
         */
        fun isAvailable(): Boolean {
            return Build.VERSION.SDK_INT >= ANDROID_16_BAKLAVA
        }
    }
}
