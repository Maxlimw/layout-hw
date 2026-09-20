package com.praktikum.playlistmaker2.data.repository

import com.praktikum.playlistmaker2.data.mapper.*
import com.praktikum.playlistmaker2.data.storage.PreferencesStorage
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class HistoryRepositoryImpl(private val storage: PreferencesStorage) : HistoryRepository {
    override fun read(): List<Track> = storage.readHistory().map { it.toDomain() }
    override fun save(tracks: List<Track>) = storage.saveHistory(tracks.map { it.toDto() })
    override fun clear() = storage.clearHistory()
}
