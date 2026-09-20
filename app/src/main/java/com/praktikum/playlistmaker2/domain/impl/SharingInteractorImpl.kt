package com.praktikum.playlistmaker2.domain.impl

import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*

class SharingInteractorImpl(private val repository: SharingRepository) : SharingInteractor {
    override fun shareApp() = repository.shareApp()
    override fun contactSupport() = repository.contactSupport()
    override fun openTerms() = repository.openTerms()
}
