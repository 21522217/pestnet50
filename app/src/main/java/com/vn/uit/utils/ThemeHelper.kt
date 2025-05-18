package com.vn.uit.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ThemeHelper {
    companion object {
        private const val THEME_PREF = "theme"
        private const val THEME_DEFAULT = "system"

        /**
         * Apply the saved theme or default theme to the application
         *
         * @param context The application context
         */
        fun applyTheme(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = sharedPreferences.getString(THEME_PREF, THEME_DEFAULT) ?: THEME_DEFAULT
            setTheme(theme)
        }

        /**
         * Set the theme based on the provided option
         *
         * @param theme The theme option: "light", "dark", or "system"
         */
        fun setTheme(theme: String) {
            when (theme) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    }
                }
            }
        }

        /**
         * Get the selected theme from preferences
         *
         * @param context The application context
         * @return The selected theme as a string: "light", "dark", or "system"
         */
        fun getSelectedTheme(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(THEME_PREF, THEME_DEFAULT) ?: THEME_DEFAULT
        }

        /**
         * Save the selected theme to preferences
         *
         * @param context The application context
         * @param theme The theme to save: "light", "dark", or "system"
         */
        fun saveSelectedTheme(context: Context, theme: String) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putString(THEME_PREF, theme).apply()
            setTheme(theme)
        }
    }
}