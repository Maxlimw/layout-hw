package com.praktikum.playlistmaker2.domain.api

import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.Cancellable

interface SearchInteractor {
    fun search(query: String, callback: (SearchResult) -> Unit): Cancellable
}
