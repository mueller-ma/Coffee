package com.github.muellerma.coffee

import android.app.Activity
import android.os.Bundle
import android.util.Log

class CoffeeInvisibleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_TOGGLE) {
            Log.d(TAG, "Received toggle")
            val started =
                ForegroundService.changeState(this, ForegroundService.Companion.STATE.TOGGLE)
            val message = if (started) R.string.turned_on else R.string.turned_off
            showToast(message)
        }

        finishAndRemoveTask()
    }

    companion object {
        private val TAG = CoffeeInvisibleActivity::class.java.simpleName
        const val ACTION_TOGGLE = "toggle"
    }
}