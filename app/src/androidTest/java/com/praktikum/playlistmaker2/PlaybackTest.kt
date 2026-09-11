package com.praktikum.playlistmaker2

import android.os.SystemClock
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PlaybackTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun playsPausesResumesCompletesAndRestarts() {
        val audio = File(context.cacheDir, "preview.wav")
        instrumentation.context.assets.open("preview.wav").use { input ->
            audio.outputStream().use { input.copyTo(it) }
        }
        val track = Track("Test", "Artist", 125000L, null, 1L, previewUrl = audio.toURI().toString())
        ActivityScenario.launch<PlayerActivity>(PlayerActivity.createIntent(context, track)).use { scenario ->
            await { scenario.onActivity { assertTrue(it.findViewById<ImageButton>(R.id.player_play).isEnabled) } }
            scenario.onActivity { it.findViewById<ImageButton>(R.id.player_play).performClick() }
            await {
                scenario.onActivity {
                    assertEquals(it.getString(R.string.player_pause), it.findViewById<ImageButton>(R.id.player_play).contentDescription)
                    assertNotEquals("00:00", it.findViewById<TextView>(R.id.player_progress).text.toString())
                }
            }
            var pausedTime = ""
            scenario.onActivity {
                it.findViewById<ImageButton>(R.id.player_play).performClick()
                pausedTime = it.findViewById<TextView>(R.id.player_progress).text.toString()
            }
            SystemClock.sleep(500)
            scenario.onActivity {
                assertEquals(pausedTime, it.findViewById<TextView>(R.id.player_progress).text.toString())
                it.findViewById<ImageButton>(R.id.player_play).performClick()
            }
            await {
                scenario.onActivity {
                    assertEquals(it.getString(R.string.player_play), it.findViewById<ImageButton>(R.id.player_play).contentDescription)
                    assertEquals("00:00", it.findViewById<TextView>(R.id.player_progress).text.toString())
                    assertEquals("02:05", it.findViewById<TextView>(R.id.player_duration).text.toString())
                }
            }
            scenario.onActivity { it.findViewById<ImageButton>(R.id.player_play).performClick() }
            await { scenario.onActivity {
                assertNotEquals("00:00", it.findViewById<TextView>(R.id.player_progress).text.toString())
            } }
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity {
                assertEquals(it.getString(R.string.player_play), it.findViewById<ImageButton>(R.id.player_play).contentDescription)
                pausedTime = it.findViewById<TextView>(R.id.player_progress).text.toString()
            }
            SystemClock.sleep(500)
            scenario.onActivity {
                assertEquals(pausedTime, it.findViewById<TextView>(R.id.player_progress).text.toString())
            }
        }
        audio.delete()
    }

    @Test
    fun missingPreviewLeavesPlayDisabled() {
        val track = Track("Old history", "Artist", 5000L, null, 2L)
        ActivityScenario.launch<PlayerActivity>(PlayerActivity.createIntent(context, track)).use { scenario ->
            scenario.onActivity {
                assertFalse(it.findViewById<ImageButton>(R.id.player_play).isEnabled)
                assertEquals("00:00", it.findViewById<TextView>(R.id.player_progress).text.toString())
            }
        }
    }

    private fun await(assertion: () -> Unit) {
        val deadline = SystemClock.uptimeMillis() + 8000
        while (true) {
            try {
                assertion()
                return
            } catch (failure: AssertionError) {
                if (SystemClock.uptimeMillis() >= deadline) throw failure
            }
            SystemClock.sleep(100)
        }
    }
}
