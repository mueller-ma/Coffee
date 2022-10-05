package com.github.muellerma.coffee.activities

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.muellerma.coffee.R
import com.github.muellerma.coffee.databinding.ActivityPreferenceBinding
import com.github.muellerma.coffee.openInBrowser
import com.github.muellerma.coffee.openSystemScreenTimeoutPermissions
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.SpecialButton


class PreferenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(binding.activityContent.id, MainSettingsFragment())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
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

            val aboutPref = getPreference("about")
            aboutPref.setOnPreferenceClickListener {
                val fragment = LibsBuilder()
                    .withAboutIconShown(true)
                    .withAboutVersionShownName(true)
                    .withSortEnabled(true)
                    .withListener(AboutButtonsListener())
                    .supportFragment()

                parentFragmentManager.commit {
                    addToBackStack("about")
                    val prefActivity = requireActivity() as PreferenceActivity
                    replace(prefActivity.binding.activityContent.id, fragment, "about")
                }
                true
            }
        }
    }
}

fun PreferenceFragmentCompat.getPreference(key: String) =
    preferenceManager.findPreference<Preference>(key)!!

class AboutButtonsListener : LibsConfiguration.LibsListener {
    override fun onExtraClicked(v: View, specialButton: SpecialButton): Boolean {
        val link = when (specialButton) {
            SpecialButton.SPECIAL1 -> "https://github.com/mueller-ma/Coffee/"
            SpecialButton.SPECIAL2 -> "https://f-droid.org/packages/com.github.muellerma.coffee/"
            SpecialButton.SPECIAL3 -> "https://crowdin.com/project/coffee-app"
        }
        link.openInBrowser(v.context)
        return true
    }

    override fun onIconClicked(v: View) {
        // no-op
    }

    override fun onIconLongClicked(v: View): Boolean {
        return false
    }

    override fun onLibraryAuthorClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryAuthorLongClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryBottomClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryBottomLongClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryContentClicked(v: View, library: Library): Boolean {
        return false
    }

    override fun onLibraryContentLongClicked(v: View, library: Library): Boolean {
        return false
    }
}