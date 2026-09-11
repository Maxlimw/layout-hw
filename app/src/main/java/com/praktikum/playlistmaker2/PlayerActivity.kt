package com.praktikum.playlistmaker2

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        private const val TRACK_EXTRA = "player_track"

        fun createIntent(context: Context, track: Track): Intent =
            Intent(context, PlayerActivity::class.java)
                .putExtra(TRACK_EXTRA, Gson().toJson(track))
    }
}
