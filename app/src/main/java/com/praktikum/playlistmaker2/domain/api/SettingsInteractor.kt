package com.praktikum.playlistmaker2.domain.api

import com.praktikum.playlistmaker2.domain.model.*

interface SettingsInteractor {
    fun getSettings(): ThemeSettings
    fun setDarkTheme(enabled: Boolean)
}
