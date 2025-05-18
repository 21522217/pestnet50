package com.vn.uit

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.vn.uit.utils.LocaleHelper
import com.vn.uit.utils.ThemeHelper

/**
 * Base activity that should be extended by all activities to support language switching
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language configuration
        val languageCode = LocaleHelper.getSelectedLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.applyTheme(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val languageCode = LocaleHelper.getSelectedLanguage(this)
        LocaleHelper.setLocale(this, languageCode)

        if (ThemeHelper.getSelectedTheme(this) == "system") {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                recreate()
            }
        }
    }
}