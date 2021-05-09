package com.github.muellerma.coffee

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.github.muellerma.coffee.databinding.ActivityMainBinding
import com.github.muellerma.coffee.databinding.DialogHelpBinding
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleCoffee.apply {
            setOnClickListener {
                ForegroundService.changeState(
                    this@MainActivity,
                    ForegroundService.Companion.STATE.TOGGLE
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

        binding.help.apply {
            setOnClickListener {
                openHelp()
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun openHelp() {
        val helpBinding = DialogHelpBinding.inflate(layoutInflater)

        helpBinding.helpDkmaButton.apply {
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

        helpBinding.helpBatteryOptimizationButton.apply {
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
            helpBinding.helpBatteryOptimizationButton.isVisible = false
            helpBinding.helpBatteryOptimizationMessage.isVisible = false
        }

        AlertDialog.Builder(this)
            .setPositiveButton(R.string.close, null)
            .setView(helpBinding.root)
            .show()
    }

    private fun getShortcutInfo(stableId: Boolean): ShortcutInfoCompat {
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
}