package com.praktikum.playlistmaker2.domain.api

import com.praktikum.playlistmaker2.domain.model.*

interface HistoryInteractor {
    fun getTracks(): List<Track>
    fun addTrack(track: Track)
    fun clear()
}
