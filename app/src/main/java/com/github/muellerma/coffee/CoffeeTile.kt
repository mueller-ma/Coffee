package com.github.muellerma.coffee

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class CoffeeTile : TileService(), ServiceStatusObserver {
    override fun onClick() {
        Log.d(TAG, "onClick()")
        ForegroundService.changeState(this, ForegroundService.Companion.STATE.TOGGLE, false)
    }

    override fun onStartListening() {
        Log.d(TAG, "onStartListening()")
        setTileState()
        (application as CoffeeApplication).observers.add(this)
        super.onStartListening()
    }

    override fun onStopListening() {
        Log.d(TAG, "onStopListening()")
        setTileState()
        (application as CoffeeApplication).observers.remove(this)
        super.onStopListening()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory()")
        super.onLowMemory()
    }

    override fun onTileAdded() {
        Log.d(TAG, "onTileAdded()")
        setTileState()
        super.onTileAdded()
    }

    override fun onTileRemoved() {
        Log.d(TAG, "onTileRemoved()")
        super.onTileRemoved()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory()")
        super.onTrimMemory(level)
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind()")
        super.onRebind(intent)
    }

    override fun onServiceStatusUpdate(status: ServiceStatus) {
        setTileState()
    }

    private fun setTileState() {
        val currentStatus = (application as CoffeeApplication).lastStatusUpdate
        Log.d(TAG, "setTileState(): running = $currentStatus")
        val tile = qsTile ?: return

        val (tileState, tileSubtitle) = when (currentStatus) {
            is ServiceStatus.Stopped -> Pair(STATE_INACTIVE, "")
            is ServiceStatus.Running -> {
                if (currentStatus.remainingSeconds == null) {
                    Pair(STATE_ACTIVE, "")
                } else {
                    Pair(STATE_ACTIVE, currentStatus.remainingSeconds.toFormattedTime())
                }
            }
        }

        tile.apply {
            state = tileState
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = tileSubtitle
            }
            updateTile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    companion object {
        private val TAG = CoffeeTile::class.java.simpleName
    }
}