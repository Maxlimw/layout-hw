package com.praktikum.playlistmaker2

import android.content.Context
import com.google.gson.Gson
import com.praktikum.playlistmaker2.data.network.ItunesApi
import com.praktikum.playlistmaker2.data.player.MediaPlayerRepository
import com.praktikum.playlistmaker2.data.repository.*
import com.praktikum.playlistmaker2.data.storage.PreferencesStorage
import com.praktikum.playlistmaker2.domain.api.*
import com.praktikum.playlistmaker2.domain.impl.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {
    private lateinit var context: Context
    private val storage by lazy {
        PreferencesStorage(context.getSharedPreferences(PreferencesStorage.PREFS_NAME, Context.MODE_PRIVATE), Gson())
    }
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun createSearchInteractor(): SearchInteractor = SearchInteractorImpl(TracksRepositoryImpl(api))
    fun createHistoryInteractor(): HistoryInteractor = HistoryInteractorImpl(HistoryRepositoryImpl(storage))
    fun createSettingsInteractor(): SettingsInteractor = SettingsInteractorImpl(SettingsRepositoryImpl(storage))
    fun createSharingInteractor(): SharingInteractor = SharingInteractorImpl(SharingRepositoryImpl(context))

    // A player belongs to one screen; never share its state between activities.
    fun createPlayerInteractor(): PlayerInteractor = PlayerInteractorImpl(MediaPlayerRepository(context))
}
