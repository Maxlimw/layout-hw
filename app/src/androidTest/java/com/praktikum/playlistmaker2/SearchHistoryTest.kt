package com.praktikum.playlistmaker2

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchHistoryTest {
    private val preferences = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("search_history_test", Context.MODE_PRIVATE)
    private lateinit var history: SearchHistory

    @Before
    fun setUp() {
        preferences.edit().clear().commit()
        history = SearchHistory(preferences)
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
    }

    @Test
    fun keepsTenMostRecentTracksAcrossInstances() {
        (1L..12L).forEach { history.addTrack(track(it)) }
        assertEquals((12L downTo 3L).toList(), SearchHistory(preferences).getTracks().map { it.trackId })
    }

    @Test
    fun repeatedTrackMovesToTopAndUpdatesAllFields() {
        (1L..10L).forEach { history.addTrack(track(it)) }
        val updated = Track("Updated", "Another artist", 123000L, "https://example.com/new.jpg", 3L)
        history.addTrack(updated)
        val restored = SearchHistory(preferences).getTracks()
        assertEquals(updated, restored.first())
        assertEquals(listOf(3L, 10L, 9L, 8L, 7L, 6L, 5L, 4L, 2L, 1L), restored.map { it.trackId })
    }

    @Test
    fun clearPersistsWithoutRemovingTheme() {
        preferences.edit().putBoolean(App.DARK_THEME_KEY, true).commit()
        history.addTrack(track(1L))
        history.clear()
        assertTrue(SearchHistory(preferences).getTracks().isEmpty())
        assertTrue(preferences.getBoolean(App.DARK_THEME_KEY, false))
    }

    @Test
    fun malformedHistoryCanBeReplaced() {
        preferences.edit().putString("search_history", "broken json").commit()
        assertTrue(history.getTracks().isEmpty())
        history.addTrack(track(1L))
        assertEquals(listOf(track(1L)), history.getTracks())
    }

    private fun track(id: Long) = Track("Track $id", "Artist", 65000L, null, id)
}
