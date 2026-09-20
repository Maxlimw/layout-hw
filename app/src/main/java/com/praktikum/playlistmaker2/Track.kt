package com.praktikum.playlistmaker2

import java.util.Locale

data class Track(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val trackId: Long,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
) {
    fun getFormattedTime(): String {
        val seconds = (trackTimeMillis ?: 0L).coerceAtLeast(0L) / 1000
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
    }

    fun getCoverArtwork(): String? = artworkUrl100
        ?.takeIf { it.isNotBlank() }
        ?.substringBeforeLast('/')
        ?.plus("/512x512bb.jpg")

    fun getReleaseYear(): String? = releaseDate
        ?.takeIf { it.length >= 4 && it.take(4).all(Char::isDigit) }
        ?.take(4)
}
