package com.praktikum.playlistmaker2.data.mapper

import com.praktikum.playlistmaker2.data.dto.*
import com.praktikum.playlistmaker2.domain.model.*

fun TrackDto.toDomain() = Track(trackName, artistName, trackTimeMillis, artworkUrl100, trackId,
    collectionName, releaseDate, primaryGenreName, country, previewUrl)

fun Track.toDto() = TrackDto(trackName, artistName, trackTimeMillis, artworkUrl100, trackId,
    collectionName, releaseDate, primaryGenreName, country, previewUrl)

fun ThemeSettingsDto.toDomain() = ThemeSettings(darkTheme)
fun ThemeSettings.toDto() = ThemeSettingsDto(darkTheme)

fun PlaybackStateDto.toDomain(): PlayerState = when (this) {
    PlaybackStateDto.PREPARING -> PlayerState.PREPARING
    PlaybackStateDto.READY -> PlayerState.READY
    PlaybackStateDto.PLAYING -> PlayerState.PLAYING
    PlaybackStateDto.PAUSED -> PlayerState.PAUSED
    PlaybackStateDto.COMPLETED -> PlayerState.COMPLETED
    PlaybackStateDto.ERROR -> PlayerState.ERROR
}
