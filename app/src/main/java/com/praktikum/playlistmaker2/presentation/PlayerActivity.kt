package com.praktikum.playlistmaker2.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.praktikum.playlistmaker2.Creator
import com.praktikum.playlistmaker2.R
import com.praktikum.playlistmaker2.domain.api.PlayerInteractor
import com.praktikum.playlistmaker2.domain.model.PlayerState
import com.praktikum.playlistmaker2.domain.model.Track
import com.praktikum.playlistmaker2.presentation.navigation.TrackExtras
import com.praktikum.playlistmaker2.presentation.util.*

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerInteractor: PlayerInteractor
    private var playerState = PlayerState.PREPARING
    private lateinit var playButton: ImageButton
    private lateinit var progressText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (playerState != PlayerState.PLAYING) return
            updateProgress()
            handler.postDelayed(this, PROGRESS_UPDATE_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val track = TrackExtras.read(intent)
        if (track == null) {
            finish()
            return
        }
        playerInteractor = Creator.createPlayerInteractor()
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_root)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        findViewById<ImageButton>(R.id.player_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        bindTrack(track)
        playButton = findViewById(R.id.player_play)
        progressText = findViewById(R.id.player_progress)
        playButton.setOnClickListener { playerInteractor.togglePlayback() }
        playerInteractor.prepare(track.previewUrl, ::renderPlayerState)
    }

    private fun renderPlayerState(state: PlayerState) {
        playerState = state
        handler.removeCallbacks(progressRunnable)
        val playing = state == PlayerState.PLAYING
        playButton.isEnabled = state in setOf(PlayerState.READY, PlayerState.PLAYING,
            PlayerState.PAUSED, PlayerState.COMPLETED)
        playButton.setImageResource(if (playing) R.drawable.ic_player_pause else R.drawable.ic_player_play)
        playButton.contentDescription = getString(when (state) {
            PlayerState.PLAYING -> R.string.player_pause
            PlayerState.UNAVAILABLE -> R.string.player_preview_unavailable
            else -> R.string.player_play
        })
        when (state) {
            PlayerState.PLAYING -> handler.post(progressRunnable)
            PlayerState.PAUSED -> updateProgress()
            else -> progressText.setText(R.string.player_initial_time)
        }
        if (state == PlayerState.ERROR) {
            Toast.makeText(this, R.string.player_preview_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProgress() {
        progressText.text = formatTime(playerInteractor.positionMillis().toLong())
    }

    override fun onPause() {
        if (::playerInteractor.isInitialized) playerInteractor.pause()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::playerInteractor.isInitialized) playerInteractor.release()
        super.onDestroy()
    }

    private fun bindTrack(track: Track) {
        findViewById<TextView>(R.id.player_track_name).text = track.trackName
        findViewById<TextView>(R.id.player_artist).text = track.artistName
        findViewById<TextView>(R.id.player_duration).text = track.getFormattedTime()
        findViewById<TextView>(R.id.player_album).text = track.collectionName
        findViewById<TextView>(R.id.player_year).text = track.getReleaseYear()
        findViewById<TextView>(R.id.player_genre).text = track.primaryGenreName
        findViewById<TextView>(R.id.player_country).text = track.country
        findViewById<Group>(R.id.player_album_group).visibility =
            if (track.collectionName.isNullOrBlank()) View.GONE else View.VISIBLE
        findViewById<Group>(R.id.player_year_group).visibility =
            if (track.getReleaseYear() == null) View.GONE else View.VISIBLE
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_player_placeholder)
            .error(R.drawable.ic_player_placeholder)
            .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.player_cover_radius)))
            .into(findViewById<ImageView>(R.id.player_cover))
    }

    companion object {
        private const val PROGRESS_UPDATE_MS = 300L
        fun createIntent(context: Context, track: Track): Intent =
            TrackExtras.write(Intent(context, PlayerActivity::class.java), track)
    }
}
