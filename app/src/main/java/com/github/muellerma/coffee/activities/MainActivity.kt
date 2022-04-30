package com.github.muellerma.coffee.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import com.github.muellerma.coffee.ForegroundService
import com.github.muellerma.coffee.R
import com.github.muellerma.coffee.databinding.ActivityMainBinding
import com.github.muellerma.coffee.showToast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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