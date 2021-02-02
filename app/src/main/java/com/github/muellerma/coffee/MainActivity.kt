package com.github.muellerma.coffee

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.toggle_coffee).apply {
            setOnClickListener {
                ForegroundService.startOrStop(
                    this@MainActivity,
                    !(application as CoffeeApplication).isRunning
                )
            }
        }

        val intent = Intent(this, CoffeeInvisibleActivity::class.java)
            .setAction(CoffeeInvisibleActivity.ACTION_TOGGLE)
        val toggleShortcut = ShortcutInfoCompat.Builder(this, "toggle")
            .setIntent(intent)
            .setShortLabel(getString(R.string.app_name))
            .setLongLabel(getString(R.string.toggle_coffee))
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut_toggle))
            .setAlwaysBadged()
            .build()

        findViewById<Button>(R.id.add_toggle_to_home).apply {
            isVisible = ShortcutManagerCompat.isRequestPinShortcutSupported(this@MainActivity)
            setOnClickListener {
                val success = ShortcutManagerCompat.requestPinShortcut(
                    this@MainActivity,
                    toggleShortcut,
                    null
                )
                val message =
                    if (success) R.string.add_toggle_to_home_success else R.string.add_toggle_to_home_no_success
                Snackbar.make(
                    this@MainActivity.findViewById(android.R.id.content),
                    message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(toggleShortcut))
        }

        findViewById<Button>(R.id.help).apply {
            setOnClickListener {
                openHelp()
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun openHelp() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_help, null)

        dialogLayout.findViewById<Button>(R.id.help_dkma_button).apply {
            setOnClickListener {
                Intent(
                    Intent.ACTION_VIEW,
                    "https://dontkillmyapp.com/?app=${URLEncoder.encode(getString(R.string.app_name), "utf-8")}"
                        .toUri()
                ).apply {
                    startActivity(this)
                }
            }
        }

        dialogLayout.findViewById<Button>(R.id.help_battery_optimization_button).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = getSystemService<PowerManager>()!!
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    isEnabled = false
                    setText(R.string.help_battery_optimization_button_already_disabled)
                }

                setOnClickListener {
                    Intent().apply {
                        action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                        context.startActivity(this)
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            dialogLayout.findViewById<Button>(R.id.help_battery_optimization_button).isVisible =
                false
            dialogLayout.findViewById<Button>(R.id.help_battery_optimization_message).isVisible =
                false
        }

        AlertDialog.Builder(this)
            .setPositiveButton(R.string.close, null)
            .setView(dialogLayout)
            .show()
    }
}