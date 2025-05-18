package com.vn.uit.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.vn.uit.R
import com.vn.uit.utils.LocaleHelper
import com.vn.uit.utils.ThemeHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val notificationPref = findPreference<androidx.preference.CheckBoxPreference>("notifications")
        val languagePref = findPreference<ListPreference>("language")
        val themePref = findPreference<ListPreference>("theme")

        // Set current language as the selected value
        val currentLang = LocaleHelper.getSelectedLanguage(requireContext())
        languagePref?.value = currentLang

        // Set current theme as the selected value
        val currentTheme = ThemeHelper.getSelectedTheme(requireContext())
        themePref?.value = currentTheme

        // Set change listener for language preference
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedLanguage = newValue.toString()
            if (selectedLanguage != currentLang) {
                // Apply the new language
                LocaleHelper.setLocale(requireContext(), selectedLanguage)

                // Restart the activity to apply language changes
                requireActivity().apply {
                    // We need to restart the entire app to ensure all fragments get the language update
                    val intent = activity?.packageManager?.getLaunchIntentForPackage(activity?.packageName ?: "")
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
            }
            true
        }

        // Set change listener for theme preference
        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedTheme = newValue.toString()
            if (selectedTheme != currentTheme) {
                // Apply the new theme
                ThemeHelper.setTheme(selectedTheme)

                // Recreate the activity to apply theme changes
                activity?.recreate()
            }
            true
        }

        // Set summaries to show the current value
        languagePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        themePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        // Example: Handling notification preference changes
        notificationPref?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                // Code to handle enabling notifications
            } else {
                // Code to handle disabling notifications
            }
            true
        }
    }
}
