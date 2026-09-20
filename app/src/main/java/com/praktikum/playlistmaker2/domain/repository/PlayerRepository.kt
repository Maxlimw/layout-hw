package com.praktikum.playlistmaker2.domain.repository

import com.praktikum.playlistmaker2.domain.model.*

interface PlayerRepository {
    fun prepare(url: String, onStateChanged: (PlayerState) -> Unit)
    fun start()
    fun pause()
    fun positionMillis(): Int
    fun release()
}
