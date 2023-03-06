package com.github.muellerma.coffee

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class CoffeeTile : TileService() {
    override fun onClick() {
        Log.d(TAG, "onClick()")
        ForegroundService.changeState(this, ForegroundService.Companion.STATE.TOGGLE, false)
    }

    override fun onStartListening() {
        Log.d(TAG, "onStartListening()")
        setTileState()
        super.onStartListening()
    }

    override fun onTileAdded() {
        Log.d(TAG, "onTileAdded()")
        setTileState()
        super.onTileAdded()
    }

    private fun setTileState() {
        val currentStatus = coffeeApp().lastStatusUpdate
        Log.d(TAG, "setTileState(): running = ${currentStatus.toString()}")
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

        fun requestTileStateUpdate(context: Context) {
            Log.d(TAG, "requestTileStateUpdate()")
            requestListeningState(context, ComponentName(context, CoffeeTile::class.java))
        }
    }

    class TileServiceStatusObserver(private val context: Context) : ServiceStatusObserver {
        override fun onServiceStatusUpdate(status: ServiceStatus) {
            requestTileStateUpdate(context)
        }

    }
}