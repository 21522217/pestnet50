package com.vn.uit.ui.settings

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.vn.uit.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val notificationPref: CheckBoxPreference? = findPreference("notifications")
        val languagePref: EditTextPreference? = findPreference("language")

        notificationPref?.isChecked = true
        languagePref?.text = "English"

        // Example: Handling preference changes
        notificationPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue == true) {
                // Code to handle enabling notifications
            } else {
                // Code to handle disabling notifications
            }
            true
        }

        // Other settings like language could be handled similarly
    }
}
