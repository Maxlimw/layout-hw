package com.praktikum.playlistmaker2.domain.repository

import com.praktikum.playlistmaker2.domain.model.*

interface SharingRepository {
    fun shareApp()
    fun contactSupport()
    fun openTerms()
}
