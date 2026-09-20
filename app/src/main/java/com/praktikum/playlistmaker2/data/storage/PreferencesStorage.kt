package com.praktikum.playlistmaker2.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.praktikum.playlistmaker2.data.dto.*

class PreferencesStorage(private val preferences: SharedPreferences, private val gson: Gson) {
    fun readHistory(): List<TrackDto> {
        val json = preferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<TrackDto?>::class.java)?.filterNotNull().orEmpty()
        } catch (_: JsonParseException) {
            emptyList()
        }
    }

    fun saveHistory(tracks: List<TrackDto>) {
        preferences.edit().putString(HISTORY_KEY, gson.toJson(tracks)).apply()
    }

    fun clearHistory() {
        preferences.edit().remove(HISTORY_KEY).apply()
    }

    fun readSettings() = ThemeSettingsDto(preferences.getBoolean(DARK_THEME_KEY, false))

    fun saveSettings(settings: ThemeSettingsDto) {
        preferences.edit().putBoolean(DARK_THEME_KEY, settings.darkTheme).apply()
    }

    companion object {
        const val PREFS_NAME = "playlist_maker_prefs"
        const val DARK_THEME_KEY = "dark_theme"
        private const val HISTORY_KEY = "search_history"
    }
}
