package com.praktikum.playlistmaker2.domain.repository

import com.praktikum.playlistmaker2.domain.model.*

interface TracksRepository {
    fun search(query: String, callback: (SearchResult) -> Unit): Cancellable
}
