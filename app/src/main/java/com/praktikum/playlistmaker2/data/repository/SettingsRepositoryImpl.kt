package com.praktikum.playlistmaker2.data.repository

import com.praktikum.playlistmaker2.data.mapper.*
import com.praktikum.playlistmaker2.data.storage.PreferencesStorage
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class SettingsRepositoryImpl(private val storage: PreferencesStorage) : SettingsRepository {
    override fun read(): ThemeSettings = storage.readSettings().toDomain()
    override fun save(settings: ThemeSettings) = storage.saveSettings(settings.toDto())
}
