package com.github.muellerma.coffee.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.github.muellerma.coffee.ForegroundService

class CoffeeInvisibleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_TOGGLE) {
            Log.d(TAG, "Received toggle")
            ForegroundService.toggleState(this, ForegroundService.Companion.STATE.TOGGLE, true)
        }

        finishAndRemoveTask()
    }

    companion object {
        private val TAG = CoffeeInvisibleActivity::class.java.simpleName
        const val ACTION_TOGGLE = "toggle"
    }
}