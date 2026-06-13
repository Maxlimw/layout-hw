package com.praktikum.playlistmaker2

data class TracksSearchResponse(
    val resultCount: Int,
    val results: List<Track>
)
