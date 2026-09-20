package com.praktikum.playlistmaker2

import com.praktikum.playlistmaker2.domain.model.Track
import com.praktikum.playlistmaker2.presentation.*
import com.praktikum.playlistmaker2.data.storage.PreferencesStorage
import com.praktikum.playlistmaker2.data.repository.HistoryRepositoryImpl
import com.praktikum.playlistmaker2.domain.impl.HistoryInteractorImpl
import com.google.gson.Gson

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class PlayerNavigationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences(PreferencesStorage.PREFS_NAME, Context.MODE_PRIVATE)
    private var savedHistory: String? = null
    private val track = Track("Navigation test track", "Test artist", 65000L, null, 999L)

    @Before
    fun saveHistory() {
        savedHistory = preferences.getString("search_history", null)
        HistoryInteractorImpl(HistoryRepositoryImpl(PreferencesStorage(preferences, Gson()))).clear()
    }

    @After
    fun restoreHistory() {
        preferences.edit().putString("search_history", savedHistory).commit()
    }

    @Test
    fun repeatedClickOpensOnlyOnePlayer() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(PlayerActivity::class.java.name, null, false)
        try {
            ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    val click = SearchActivity::class.java.getDeclaredMethod("onTrackClick", Track::class.java)
                    click.isAccessible = true
                    click.invoke(activity, track)
                    click.invoke(activity, track)
                }
                onView(withId(R.id.player_track_name)).check(matches(withText(track.trackName)))
                assertEquals(1, monitor.hits)
                pressBack()
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    @Test
    fun opensFromResultsAndToolbarBackReturnsToResults() {
        ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.search_edit_text).setText("Navigation")
                activity.findViewById<RecyclerView>(R.id.tracks_recycler_view).apply {
                    visibility = View.VISIBLE
                    (adapter as TracksAdapter).setTracks(listOf(track))
                }
            }
            onView(withText(track.trackName)).perform(click())
            onView(withId(R.id.player_track_name)).check(matches(withText(track.trackName)))
            onView(withId(R.id.player_back)).perform(click())
            onView(withId(R.id.tracks_recycler_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun opensFromHistoryAndSystemBackReturnsToHistory() {
        HistoryInteractorImpl(HistoryRepositoryImpl(PreferencesStorage(preferences, Gson()))).addTrack(track)
        ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.search_edit_text).requestFocus()
            }
            onView(withText(track.trackName)).perform(click())
            onView(withId(R.id.player_track_name)).check(matches(withText(track.trackName)))
            pressBack()
            onView(withId(R.id.history_container)).check(matches(isDisplayed()))
        }
    }
}
