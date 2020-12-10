package com.github.muellerma.coffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, CoffeeInvisibleActivity::class.java)
            .setAction(CoffeeInvisibleActivity.ACTION_TOGGLE)
        val toggleButton = ShortcutInfoCompat.Builder(this, "toggle")
            .setIntent(intent)
            .setShortLabel(getString(R.string.app_name))
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut_toggle))
            .setAlwaysBadged()
            .build()

        findViewById<Button>(R.id.add_toggle_to_home).apply {
            isVisible = ShortcutManagerCompat.isRequestPinShortcutSupported(this@MainActivity)
            setOnClickListener {
                val success = ShortcutManagerCompat.requestPinShortcut(this@MainActivity, toggleButton, null)
                val message = if (success) R.string.add_toggle_to_home_success else R.string.add_toggle_to_home_no_success
                Snackbar.make(this@MainActivity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}