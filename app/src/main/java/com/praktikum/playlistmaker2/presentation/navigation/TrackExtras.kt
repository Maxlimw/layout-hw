package com.praktikum.playlistmaker2.presentation.navigation

import android.content.Intent
import com.praktikum.playlistmaker2.domain.model.Track

// Screen navigation is a UI concern; network/storage DTOs stay in the data layer.
object TrackExtras {
    fun write(intent: Intent, track: Track): Intent = intent.apply {
        putExtra("trackId", track.trackId)
        putExtra("trackName", track.trackName)
        putExtra("artistName", track.artistName)
        track.trackTimeMillis?.let { putExtra("trackTimeMillis", it) }
        putExtra("artworkUrl100", track.artworkUrl100)
        putExtra("collectionName", track.collectionName)
        putExtra("releaseDate", track.releaseDate)
        putExtra("primaryGenreName", track.primaryGenreName)
        putExtra("country", track.country)
        putExtra("previewUrl", track.previewUrl)
    }

    fun read(intent: Intent): Track? {
        if (!intent.hasExtra("trackId")) return null
        return Track(
            trackName = intent.getStringExtra("trackName"),
            artistName = intent.getStringExtra("artistName"),
            trackTimeMillis = if (intent.hasExtra("trackTimeMillis")) intent.getLongExtra("trackTimeMillis", 0L) else null,
            artworkUrl100 = intent.getStringExtra("artworkUrl100"),
            trackId = intent.getLongExtra("trackId", 0L),
            collectionName = intent.getStringExtra("collectionName"),
            releaseDate = intent.getStringExtra("releaseDate"),
            primaryGenreName = intent.getStringExtra("primaryGenreName"),
            country = intent.getStringExtra("country"),
            previewUrl = intent.getStringExtra("previewUrl")
        )
    }
}
