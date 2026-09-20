package com.praktikum.playlistmaker2

import com.praktikum.playlistmaker2.presentation.*

import android.os.SystemClock
import android.view.View
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.*
import org.junit.Test
import com.praktikum.playlistmaker2.domain.api.SearchInteractor
import com.praktikum.playlistmaker2.domain.model.SearchResult
import com.praktikum.playlistmaker2.domain.repository.Cancellable

class SearchDebounceTest {
    @Test
    fun waitsAfterLastCharacterAndIgnoresCancelledResponse() {
        val api = FakeApi()
        ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                SearchActivity::class.java.getDeclaredField("searchInteractor").apply {
                    isAccessible = true
                    set(activity, api)
                }
                activity.findViewById<EditText>(R.id.search_edit_text).setText("beat")
            }
            SystemClock.sleep(1200)
            scenario.onActivity {
                assertTrue(api.calls.isEmpty())
                it.findViewById<EditText>(R.id.search_edit_text).setText("beatles")
            }
            SystemClock.sleep(1200)
            scenario.onActivity { assertTrue(api.calls.isEmpty()) }
            SystemClock.sleep(1100)
            scenario.onActivity {
                assertEquals(listOf("beatles"), api.queries)
                assertEquals(View.VISIBLE, it.findViewById<View>(R.id.search_progress).visibility)
                it.findViewById<EditText>(R.id.search_edit_text).setText("queen")
                assertTrue(api.calls.first().isCanceled)
                api.calls.first().respond()
                assertEquals(View.GONE, it.findViewById<View>(R.id.placeholder_container).visibility)
            }
            SystemClock.sleep(2300)
            scenario.onActivity {
                assertEquals(listOf("beatles", "queen"), api.queries)
                api.calls.last().respond()
                assertEquals(View.GONE, it.findViewById<View>(R.id.search_progress).visibility)
                assertEquals(View.VISIBLE, it.findViewById<View>(R.id.placeholder_container).visibility)
            }
        }
    }

    @Test
    fun clearAndDestroyCancelPendingSearch() {
        val api = FakeApi()
        val scenario = ActivityScenario.launch(SearchActivity::class.java)
        scenario.onActivity {
            SearchActivity::class.java.getDeclaredField("searchInteractor").apply {
                isAccessible = true
                set(it, api)
            }
            it.findViewById<EditText>(R.id.search_edit_text).setText("discard")
            it.findViewById<EditText>(R.id.search_edit_text).setText("")
        }
        SystemClock.sleep(2300)
        scenario.onActivity {
            assertTrue(api.calls.isEmpty())
            it.findViewById<EditText>(R.id.search_edit_text).setText("closed")
        }
        scenario.close()
        SystemClock.sleep(2300)
        assertTrue(api.calls.isEmpty())
    }

    private class FakeApi : SearchInteractor {
        val calls = mutableListOf<FakeCall>()
        val queries = mutableListOf<String>()
        override fun search(query: String, callback: (SearchResult) -> Unit): Cancellable {
            queries.add(query)
            return FakeCall(callback).also { calls.add(it) }
        }
    }

    private class FakeCall(private val callback: (SearchResult) -> Unit) : Cancellable {
        var isCanceled = false
            private set
        fun respond() {
            callback(SearchResult.Success(emptyList()))
        }
        override fun cancel() { isCanceled = true }
    }
}
