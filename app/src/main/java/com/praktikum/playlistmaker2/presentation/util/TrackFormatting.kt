package com.praktikum.playlistmaker2.presentation.util

import com.praktikum.playlistmaker2.domain.model.Track
import java.util.Locale

fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1000
    return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
}

fun Track.getFormattedTime(): String = formatTime(trackTimeMillis ?: 0L)

fun Track.getCoverArtwork(): String? = artworkUrl100
    ?.takeIf { it.isNotBlank() }
    ?.substringBeforeLast('/')
    ?.plus("/512x512bb.jpg")

fun Track.getReleaseYear(): String? = releaseDate
    ?.takeIf { it.length >= 4 && it.take(4).all(Char::isDigit) }
    ?.take(4)
