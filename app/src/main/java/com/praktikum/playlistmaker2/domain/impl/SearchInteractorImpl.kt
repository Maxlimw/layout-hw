package com.praktikum.playlistmaker2.domain.impl

import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class SearchInteractorImpl(private val repository: TracksRepository) : SearchInteractor {
    override fun search(query: String, callback: (SearchResult) -> Unit): Cancellable {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            callback(SearchResult.Success(emptyList()))
            return Cancellable {}
        }
        return repository.search(trimmedQuery, callback)
    }
}
