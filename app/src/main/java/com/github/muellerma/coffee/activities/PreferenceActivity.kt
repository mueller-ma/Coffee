package com.github.muellerma.coffee.activities

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.muellerma.coffee.R
import com.github.muellerma.coffee.databinding.ActivityPreferenceBinding
import com.github.muellerma.coffee.openSystemScreenTimeoutPermissions


class PreferenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.commit {
            add(binding.activityContent.id, MainSettingsFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class MainSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)

            val allowDimmingPref = getPreference("allow_dimming")
            val alternateModePref = getPreference("alternate_mode")
            alternateModePref.setOnPreferenceChangeListener { pref, newValue ->
                val context = pref.context
                val enabled = newValue as Boolean

                if (enabled &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.System.canWrite(context)) {
                        context.openSystemScreenTimeoutPermissions()
                }

                allowDimmingPref.isEnabled = !enabled

                return@setOnPreferenceChangeListener true
            }
        }
    }
}

fun PreferenceFragmentCompat.getPreference(key: String) =
    preferenceManager.findPreference<Preference>(key)!!