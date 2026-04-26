package com.praktikum.playlistmaker2

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        const val PREFS_NAME = "playlist_maker_prefs"
        const val DARK_THEME_KEY = "dark_theme"
    }

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val darkThemeEnabled = sharedPreferences.getBoolean(DARK_THEME_KEY, false)

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}