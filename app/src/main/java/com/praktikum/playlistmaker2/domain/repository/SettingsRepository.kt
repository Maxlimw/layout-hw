package com.praktikum.playlistmaker2.domain.repository

import com.praktikum.playlistmaker2.domain.model.*

interface SettingsRepository {
    fun read(): ThemeSettings
    fun save(settings: ThemeSettings)
}
