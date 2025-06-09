package com.vn.uit

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.vn.uit.utils.LocaleHelper
import java.util.Locale
import com.vn.uit.utils.ThemeHelper

class PestClassificationApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize language configuration on app startup
        val languageCode = LocaleHelper.getSelectedLanguage(this)
        applyLanguage(languageCode)
        ThemeHelper.applyTheme(this)
    }

    override fun attachBaseContext(base: Context) {
        // Apply the saved language when application context is created
        val languageCode = LocaleHelper.getSelectedLanguage(base)
        val context = LocaleHelper.setLocale(base, languageCode)
        super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Apply saved language during configuration changes
        val languageCode = LocaleHelper.getSelectedLanguage(this)
        applyLanguage(languageCode)
    }

    private fun applyLanguage(languageCode: String) {
        // Create locale from language code
        val locale = when (languageCode) {
            "vi" -> Locale("vi") // Vietnamese
            "zh" -> Locale("zh") // Chinese
            "ru" -> Locale("ru")
            else -> Locale("en") // English (default)
        }

        // Set default locale
        Locale.setDefault(locale)

        // Update configuration
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}