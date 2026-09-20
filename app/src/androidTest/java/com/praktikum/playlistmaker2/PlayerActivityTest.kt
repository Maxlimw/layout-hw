package com.praktikum.playlistmaker2

import com.praktikum.playlistmaker2.domain.model.Track
import com.praktikum.playlistmaker2.presentation.*

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PlayerActivityTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val track = Track("Yesterday", "The Beatles", 125000L, null, 1L,
        "Help!", "1965-08-06T12:00:00Z", "Rock", "GBR")

    @Test
    fun displaysTrackAfterRecreationAndReturnFromBackground() {
        ActivityScenario.launch<PlayerActivity>(PlayerActivity.createIntent(context, track)).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals("Yesterday", activity.findViewById<TextView>(R.id.player_track_name).text.toString())
                assertEquals("Help!", activity.findViewById<TextView>(R.id.player_album).text.toString())
                assertEquals("1965", activity.findViewById<TextView>(R.id.player_year).text.toString())
                assertEquals("02:05", activity.findViewById<TextView>(R.id.player_duration).text.toString())
                assertEquals("Rock", activity.findViewById<TextView>(R.id.player_genre).text.toString())
                assertEquals("GBR", activity.findViewById<TextView>(R.id.player_country).text.toString())
            }
            captureScreenshot("player-light.png")
            onView(withId(R.id.player_root)).perform(swipeUp())
            onView(withId(R.id.player_country)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun darkThemeKeepsTrackVisible() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val previousMode = AppCompatDelegate.getDefaultNightMode()
        try {
            instrumentation.runOnMainSync {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            ActivityScenario.launch<PlayerActivity>(PlayerActivity.createIntent(context, track)).use { scenario ->
                scenario.onActivity { activity ->
                    assertEquals("Yesterday", activity.findViewById<TextView>(R.id.player_track_name).text.toString())
                    assertEquals(activity.getColor(R.color.yp_white),
                        activity.findViewById<TextView>(R.id.player_track_name).currentTextColor)
                }
                captureScreenshot("player-dark.png")
            }
        } finally {
            instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(previousMode) }
        }
    }

    private fun captureScreenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("screenshots") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Window transitions continue after the main thread becomes idle.
        android.os.SystemClock.sleep(700)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        File(context.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test
    fun missingAlbumAndYearHideBothLabelsAndValues() {
        val oldTrack = track.copy(collectionName = null, releaseDate = null)
        ActivityScenario.launch<PlayerActivity>(PlayerActivity.createIntent(context, oldTrack)).use { scenario ->
            scenario.onActivity { activity ->
                listOf(R.id.player_album, R.id.player_album_label, R.id.player_year, R.id.player_year_label).forEach {
                    assertEquals(View.GONE, activity.findViewById<View>(it).visibility)
                }
            }
        }
    }

    @Test
    fun missingTrackClosesPlayerSafely() {
        ActivityScenario.launch<PlayerActivity>(Intent(context, PlayerActivity::class.java)).use {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }
}
