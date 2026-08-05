package com.github.muellerma.coffee.tiles

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.github.muellerma.coffee.R
import com.github.muellerma.coffee.activities.CoffeeInvisibleActivity
import com.github.muellerma.coffee.ServiceStatus
import com.github.muellerma.coffee.coffeeApp
import com.github.muellerma.coffee.toFormattedTime

@RequiresApi(Build.VERSION_CODES.N)
abstract class AbstractTile : TileService() {
    @CallSuper
    override fun onClick() {
        Log.d(TAG, "onClick()")
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Delaying tile state update for 1 second")
            setTileState()
        }, 500)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
        setTileState()
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
        Log.d(TAG, "setTileState(): running = $currentStatus")
        val tile = qsTile ?: return

        val (tileState, tileSubtitle) = when (currentStatus) {
            is ServiceStatus.Stopped -> Pair(Tile.STATE_INACTIVE, "")
            is ServiceStatus.Running -> {
                if (currentStatus.remaining == null) {
                    Pair(Tile.STATE_ACTIVE, "")
                } else {
                    Pair(Tile.STATE_ACTIVE, currentStatus.remaining.toFormattedTime())
                }
            }
        }

        tile.apply {
            state = tileState
            label = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = tileSubtitle
            }
            updateTile()
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Suppress("DEPRECATION")
    protected fun launchToggleActivity() {
        val intent = CoffeeInvisibleActivity.toggleIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    companion object {
        private val TAG = AbstractTile::class.java.simpleName
    }
}
