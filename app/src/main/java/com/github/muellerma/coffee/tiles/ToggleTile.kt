package com.github.muellerma.coffee.tiles

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.github.muellerma.coffee.ForegroundService
import com.github.muellerma.coffee.ServiceStatus
import com.github.muellerma.coffee.ServiceStatusObserver

@RequiresApi(Build.VERSION_CODES.N)
class ToggleTile : AbstractTile() {
    override fun onClick() {
        Log.d(TAG, "onClick()")
        ForegroundService.changeState(this, ForegroundService.Companion.STATE.TOGGLE, false)
        super.onClick()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    companion object {
        private val TAG = ToggleTile::class.java.simpleName

        fun requestTileStateUpdate(context: Context) {
            Log.d(TAG, "requestTileStateUpdate()")
            try {
                requestListeningState(context, ComponentName(context, ToggleTile::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Error when calling requestListeningState()", e)
            }
        }
    }

    class TileServiceStatusObserver(private val context: Context) : ServiceStatusObserver {
        override fun onServiceStatusUpdate(status: ServiceStatus) {
            requestTileStateUpdate(context)
        }

    }
}