package com.praktikum.playlistmaker2.domain.api

import com.praktikum.playlistmaker2.domain.model.*

interface PlayerInteractor {
    fun prepare(url: String?, onStateChanged: (PlayerState) -> Unit)
    fun togglePlayback()
    fun pause()
    fun positionMillis(): Int
    fun release()
}
