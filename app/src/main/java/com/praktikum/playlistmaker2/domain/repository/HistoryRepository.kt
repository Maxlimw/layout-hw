package com.praktikum.playlistmaker2.domain.repository

import com.praktikum.playlistmaker2.domain.model.*

interface HistoryRepository {
    fun read(): List<Track>
    fun save(tracks: List<Track>)
    fun clear()
}
