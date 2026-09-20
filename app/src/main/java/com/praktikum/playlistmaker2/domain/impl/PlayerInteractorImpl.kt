package com.praktikum.playlistmaker2.domain.impl

import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class PlayerInteractorImpl(private val repository: PlayerRepository) : PlayerInteractor {
    private var state = PlayerState.PREPARING

    override fun prepare(url: String?, onStateChanged: (PlayerState) -> Unit) {
        if (url.isNullOrBlank()) {
            repository.release()
            state = PlayerState.UNAVAILABLE
            onStateChanged(state)
            return
        }
        state = PlayerState.PREPARING
        onStateChanged(state)
        repository.prepare(url) {
            state = it
            onStateChanged(it)
        }
    }

    override fun togglePlayback() {
        when (state) {
            PlayerState.PLAYING -> pause()
            PlayerState.READY, PlayerState.PAUSED, PlayerState.COMPLETED -> repository.start()
            else -> Unit
        }
    }

    override fun pause() {
        if (state == PlayerState.PLAYING) repository.pause()
    }

    override fun positionMillis(): Int = repository.positionMillis()
    override fun release() {
        state = PlayerState.UNAVAILABLE
        repository.release()
    }
}
