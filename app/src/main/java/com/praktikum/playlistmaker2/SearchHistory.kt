package com.praktikum.playlistmaker2

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParseException

class SearchHistory(private val preferences: SharedPreferences) {
    private val gson = Gson()

    fun getTracks(): List<Track> {
        val json = preferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<Track?>::class.java)
                ?.filterNotNull()
                ?.distinctBy { it.trackId }
                ?.take(MAX_TRACKS)
                ?: emptyList()
        } catch (_: JsonParseException) {
            emptyList()
        }
    }

    fun addTrack(track: Track) {
        val tracks = (listOf(track) + getTracks().filter { it.trackId != track.trackId })
            .take(MAX_TRACKS)
        preferences.edit().putString(HISTORY_KEY, gson.toJson(tracks)).apply()
    }

    fun clear() {
        preferences.edit().remove(HISTORY_KEY).apply()
    }

    private companion object {
        const val HISTORY_KEY = "search_history"
        const val MAX_TRACKS = 10
    }
}
