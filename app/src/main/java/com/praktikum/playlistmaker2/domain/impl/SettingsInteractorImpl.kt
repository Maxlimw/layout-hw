package com.praktikum.playlistmaker2.domain.impl

import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class SettingsInteractorImpl(private val repository: SettingsRepository) : SettingsInteractor {
    override fun getSettings(): ThemeSettings = repository.read()
    override fun setDarkTheme(enabled: Boolean) = repository.save(ThemeSettings(enabled))
}
