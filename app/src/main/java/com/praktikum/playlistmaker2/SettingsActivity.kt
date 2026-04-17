package com.praktikum.playlistmaker2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SettingsActivity", "onCreate")
        setContentView(R.layout.aktivity_settings) // имя вашего layout из прошлой темы
    }
}