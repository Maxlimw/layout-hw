package com.praktikum.playlistmaker2.domain.impl

import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class HistoryInteractorImpl(private val repository: HistoryRepository) : HistoryInteractor {
    override fun getTracks(): List<Track> = repository.read().distinctBy { it.trackId }.take(MAX_TRACKS)

    override fun addTrack(track: Track) {
        repository.save((listOf(track) + getTracks().filter { it.trackId != track.trackId }).take(MAX_TRACKS))
    }

    override fun clear() = repository.clear()

    private companion object {
        const val MAX_TRACKS = 10
    }
}
