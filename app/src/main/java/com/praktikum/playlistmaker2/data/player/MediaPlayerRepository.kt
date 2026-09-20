package com.praktikum.playlistmaker2.data.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.praktikum.playlistmaker2.data.dto.PlaybackStateDto
import com.praktikum.playlistmaker2.data.mapper.toDomain
import com.praktikum.playlistmaker2.domain.model.PlayerState
import com.praktikum.playlistmaker2.domain.repository.PlayerRepository
import java.io.IOException

class MediaPlayerRepository(context: Context) : PlayerRepository {
    private val context = context.applicationContext
    private var player: MediaPlayer? = null
    private var state = PlaybackStateDto.PREPARING
    private var listener: ((PlayerState) -> Unit)? = null

    override fun prepare(url: String, onStateChanged: (PlayerState) -> Unit) {
        release()
        listener = onStateChanged
        val current = MediaPlayer()
        player = current
        publish(PlaybackStateDto.PREPARING)
        current.setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        current.setOnPreparedListener {
            if (player === it) publish(PlaybackStateDto.READY)
        }
        current.setOnCompletionListener {
            if (player === it) publish(PlaybackStateDto.COMPLETED)
        }
        current.setOnErrorListener { failedPlayer, _, _ ->
            if (player === failedPlayer) fail()
            true
        }
        try {
            current.setDataSource(context, Uri.parse(url))
            current.prepareAsync()
        } catch (_: IOException) {
            fail()
        } catch (_: IllegalArgumentException) {
            fail()
        } catch (_: IllegalStateException) {
            fail()
        } catch (_: SecurityException) {
            fail()
        }
    }

    override fun start() {
        if (state !in setOf(PlaybackStateDto.READY, PlaybackStateDto.PAUSED, PlaybackStateDto.COMPLETED)) return
        val current = player ?: return
        try {
            current.start()
            publish(PlaybackStateDto.PLAYING)
        } catch (_: IllegalStateException) {
            fail()
        }
    }

    override fun pause() {
        if (state != PlaybackStateDto.PLAYING) return
        try {
            player?.pause()
            publish(PlaybackStateDto.PAUSED)
        } catch (_: IllegalStateException) {
            fail()
        }
    }

    override fun positionMillis(): Int {
        if (state == PlaybackStateDto.PREPARING || state == PlaybackStateDto.ERROR) return 0
        return try {
            player?.currentPosition ?: 0
        } catch (_: IllegalStateException) {
            0
        }
    }

    override fun release() {
        listener = null
        releasePlayer()
        state = PlaybackStateDto.PREPARING
    }

    private fun releasePlayer() {
        val previous = player
        player = null
        previous?.setOnPreparedListener(null)
        previous?.setOnCompletionListener(null)
        previous?.setOnErrorListener(null)
        previous?.release()
    }

    private fun publish(value: PlaybackStateDto) {
        state = value
        listener?.invoke(value.toDomain())
    }

    private fun fail() {
        releasePlayer()
        publish(PlaybackStateDto.ERROR)
    }
}
