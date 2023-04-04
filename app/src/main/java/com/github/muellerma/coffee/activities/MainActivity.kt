package com.github.muellerma.coffee.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import com.github.muellerma.coffee.*
import com.github.muellerma.coffee.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), ServiceStatusObserver {
    private lateinit var application: CoffeeApplication
    private lateinit var binding: ActivityMainBinding
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::handleNotificationPermission
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        application = getApplication() as CoffeeApplication
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.toggleCoffee.apply {
            setOnClickListener {
                ForegroundService.changeState(
                    this@MainActivity,
                    ForegroundService.Companion.STATE.TOGGLE,
                    true
                )
            }
        }

        binding.addToggleToHome.apply {
            isVisible = ShortcutManagerCompat.isRequestPinShortcutSupported(this@MainActivity)
            setOnClickListener {
                val success = ShortcutManagerCompat.requestPinShortcut(
                    this@MainActivity,
                    getShortcutInfo(false),
                    null
                )
                val message = if (success) R.string.add_toggle_to_home_success else R.string.add_toggle_to_home_no_success
                showToast(message)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(getShortcutInfo(true)))
        }

        binding.settings.apply {
            setOnClickListener {
                Intent(this@MainActivity, PreferenceActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
        requestNotificationPermissionIfRequired()
        application.observers.add(this)
        onServiceStatusUpdate(application.lastStatusUpdate)
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        application.observers.remove(this)
    }

    override fun onServiceStatusUpdate(status: ServiceStatus) {
        binding.status.text = when (status) {
            is ServiceStatus.Stopped -> getString(R.string.turned_off)
            is ServiceStatus.Running -> {
                if (status.remaining == null) {
                    getString(R.string.turned_on)
                } else {
                    getString(R.string.turned_on_remaining, status.remaining.toFormattedTime())
                }
            }
        }
    }

    private fun requestNotificationPermissionIfRequired() {
        Log.d(TAG, "requestNotificationPermissionIfRequired()")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val hasPermission = hasPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        if (!hasPermission) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.notification_permission)
                .setPositiveButton(R.string.grant) { _, _ ->
                    notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun handleNotificationPermission(granted: Boolean) {
        Log.d(TAG, "handleNotificationPermission($granted)")
        if (!granted) {
            requestNotificationPermissionIfRequired()
        }
    }

    private fun getShortcutInfo(stableId: Boolean): ShortcutInfoCompat {
        Log.d(TAG, "getShortcutInfo($stableId)")
        val id = if (stableId) "toggle" else "toggle-${System.currentTimeMillis()}"
        val intent = Intent(this, CoffeeInvisibleActivity::class.java)
            .setAction(CoffeeInvisibleActivity.ACTION_TOGGLE)
        return ShortcutInfoCompat.Builder(this, id)
            .setIntent(intent)
            .setShortLabel(getString(R.string.app_name))
            .setLongLabel(getString(R.string.toggle_coffee))
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut_toggle))
            .setAlwaysBadged()
            .build()
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}