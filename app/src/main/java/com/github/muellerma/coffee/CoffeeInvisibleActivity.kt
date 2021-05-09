package com.github.muellerma.coffee

import android.app.Activity
import android.os.Bundle
import android.util.Log

class CoffeeInvisibleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_TOGGLE) {
            Log.d(TAG, "Received toggle")
            ForegroundService.changeState(this, ForegroundService.Companion.STATE.TOGGLE, true)
        }

        finishAndRemoveTask()
    }

    companion object {
        private val TAG = CoffeeInvisibleActivity::class.java.simpleName
        const val ACTION_TOGGLE = "toggle"
    }
}