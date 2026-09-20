package com.praktikum.playlistmaker2.domain.model

sealed interface SearchResult {
    data class Success(val tracks: List<Track>) : SearchResult
    data object Error : SearchResult
}
