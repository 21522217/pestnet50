package com.vn.uit.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Utility class for handling localization in the app
 */
object LocaleHelper {

    private const val LANGUAGE_PREFERENCE = "selected_language"
    private const val PREF_NAME = "app_prefs"

    /**
     * Updates the locale for a context
     *
     * @param context The context to update
     * @param languageCode The language code to set
     * @return Context with updated locale configuration
     */
    fun setLocale(context: Context, languageCode: String): Context {
        // Save selected language
        saveSelectedLanguage(context, languageCode)

        // Create locale from language code
        val locale = when (languageCode) {
            "vi" -> Locale("vi") // Vietnamese
            "zh" -> Locale("zh") // Chinese
            else -> Locale("en") // English (default)
        }

        // Update locale configuration
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Gets the current language code from preferences
     */
    fun getSelectedLanguage(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(LANGUAGE_PREFERENCE, Locale.getDefault().language) ?: "en"
    }

    /**
     * Saves the selected language to preferences
     */
    private fun saveSelectedLanguage(context: Context, languageCode: String) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(LANGUAGE_PREFERENCE, languageCode).apply()
    }
}