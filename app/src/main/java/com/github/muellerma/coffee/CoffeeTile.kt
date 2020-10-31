package com.github.muellerma.coffee

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import android.util.Log

class CoffeeTile : TileService() {
    override fun onClick() {
        Log.d(TAG, "onClick()")
        ForegroundService.startOrStop(application, this, !(application as CoffeeApplication).isRunning)
    }

    override fun onStartListening() {
        Log.d(TAG, "onStartListening()")
        super.onStartListening()
        setTileState()
    }

    override fun onStopListening() {
        Log.d(TAG, "onStopListening()")
        super.onStopListening()
        setTileState()
    }

    private fun setTileState() {
        val isRunning = (application as CoffeeApplication).isRunning
        Log.d(TAG, "setTileState: running = $isRunning")
        val tile = qsTile ?: return
        tile.apply {
            state = if (isRunning) STATE_ACTIVE else STATE_INACTIVE
            updateTile()
        }
    }

    companion object {
        private val TAG = CoffeeTile::class.java.simpleName

        fun requestTileStateUpdate(context: Context) {
            Log.d(TAG, "requestTileStateUpdate()")
            requestListeningState(context, ComponentName(context, CoffeeTile::class.java))
        }
    }
}