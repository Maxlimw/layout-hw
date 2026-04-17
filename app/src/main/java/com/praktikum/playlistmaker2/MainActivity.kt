package com.praktikum.playlistmaker2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val searchButton = findViewById<Button>(R.id.button_search)
        val mediaButton = findViewById<Button>(R.id.button_media)
        val settingsButton = findViewById<Button>(R.id.button_settings)

        // Обработчики нажатий
        searchButton.setOnClickListener {
            Log.d("MainActivity", "Search button clicked")
            startActivity(Intent(this, SearchActivity::class.java))
        }

        mediaButton.setOnClickListener {
            Log.d("MainActivity", "Search button clicked")
            startActivity(Intent(this, MediaActivity::class.java))
        }

        settingsButton.setOnClickListener {
            Log.d("MainActivity", "Settings button clicked")
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

