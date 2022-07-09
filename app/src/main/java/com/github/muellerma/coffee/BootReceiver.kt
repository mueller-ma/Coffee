package com.github.muellerma.coffee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoffeeTile.requestTileStateUpdate(context)
    }
}