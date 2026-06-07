package com.praktikum.playlistmaker2

import java.text.SimpleDateFormat
import java.util.Locale

data class Track(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
) {
    fun getFormattedTime(): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis ?: 0L)
}
