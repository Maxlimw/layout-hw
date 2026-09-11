package com.praktikum.playlistmaker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import java.io.IOException
import java.util.Locale
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.JsonParseException

class PlayerActivity : AppCompatActivity() {
    private enum class PlayerState { PREPARING, READY, PLAYING, PAUSED, COMPLETED, ERROR }

    private var playerState = PlayerState.PREPARING
    private var mediaPlayer: MediaPlayer? = null
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
        val track = readTrack()
        if (track == null) {
            finish()
            return
        }

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
        playButton.setOnClickListener {
            when (playerState) {
                PlayerState.PLAYING -> pausePlayback()
                PlayerState.READY, PlayerState.PAUSED, PlayerState.COMPLETED -> startPlayback()
                else -> Unit
            }
        }
        preparePlayer(track.previewUrl)
    }

    private fun preparePlayer(previewUrl: String?) {
        playButton.isEnabled = false
        if (previewUrl.isNullOrBlank()) {
            playerState = PlayerState.ERROR
            playButton.contentDescription = getString(R.string.player_preview_unavailable)
            return
        }
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        player.setOnPreparedListener {
            if (mediaPlayer !== it) return@setOnPreparedListener
            playerState = PlayerState.READY
            playButton.isEnabled = true
        }
        player.setOnCompletionListener {
            if (mediaPlayer !== it) return@setOnCompletionListener
            handler.removeCallbacks(progressRunnable)
            playerState = PlayerState.COMPLETED
            showPlayButton()
            progressText.setText(R.string.player_initial_time)
        }
        player.setOnErrorListener { failedPlayer, _, _ ->
            if (mediaPlayer === failedPlayer) handlePlayerError()
            true
        }
        try {
            player.setDataSource(this, Uri.parse(previewUrl))
            player.prepareAsync()
        } catch (_: IOException) {
            handlePlayerError()
        } catch (_: IllegalArgumentException) {
            handlePlayerError()
        } catch (_: IllegalStateException) {
            handlePlayerError()
        } catch (_: SecurityException) {
            handlePlayerError()
        }
    }

    private fun startPlayback() {
        val player = mediaPlayer ?: return
        // MediaPlayer restarts a completed clip from the beginning.
        player.start()
        playerState = PlayerState.PLAYING
        playButton.setImageResource(R.drawable.ic_player_pause)
        playButton.contentDescription = getString(R.string.player_pause)
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun pausePlayback() {
        if (playerState != PlayerState.PLAYING) return
        mediaPlayer?.pause()
        playerState = PlayerState.PAUSED
        handler.removeCallbacks(progressRunnable)
        updateProgress()
        showPlayButton()
    }

    private fun updateProgress() {
        val seconds = (mediaPlayer?.currentPosition ?: 0) / 1000
        progressText.text = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
    }

    private fun showPlayButton() {
        playButton.setImageResource(R.drawable.ic_player_play)
        playButton.contentDescription = getString(R.string.player_play)
    }

    private fun handlePlayerError() {
        handler.removeCallbacks(progressRunnable)
        playerState = PlayerState.ERROR
        showPlayButton()
        playButton.isEnabled = false
        progressText.setText(R.string.player_initial_time)
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(this, R.string.player_preview_error, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        pausePlayback()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun readTrack(): Track? {
        val json = intent.getStringExtra(TRACK_EXTRA) ?: return null
        return try {
            Gson().fromJson(json, Track::class.java)
        } catch (_: JsonParseException) {
            null
        }
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
        private const val TRACK_EXTRA = "player_track"

        fun createIntent(context: Context, track: Track): Intent =
            Intent(context, PlayerActivity::class.java)
                .putExtra(TRACK_EXTRA, Gson().toJson(track))
    }
}
