package com.praktikum.playlistmaker2

import android.os.SystemClock
import android.view.View
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import okhttp3.Request
import okio.Timeout
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchDebounceTest {
    @Test
    fun waitsAfterLastCharacterAndIgnoresCancelledResponse() {
        val api = FakeApi()
        ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                SearchActivity::class.java.getDeclaredField("itunesApi").apply {
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
            SearchActivity::class.java.getDeclaredField("itunesApi").apply {
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

    private class FakeApi : ItunesApi {
        val calls = mutableListOf<FakeCall>()
        val queries = mutableListOf<String>()
        override fun search(text: String): Call<TracksSearchResponse> {
            queries.add(text)
            return FakeCall().also { calls.add(it) }
        }
    }

    private class FakeCall : Call<TracksSearchResponse> {
        private var callback: Callback<TracksSearchResponse>? = null
        private var cancelled = false
        fun respond() {
            callback?.onResponse(this, Response.success(TracksSearchResponse(0, emptyList())))
        }
        override fun enqueue(callback: Callback<TracksSearchResponse>) { this.callback = callback }
        override fun cancel() { cancelled = true }
        override fun isCanceled() = cancelled
        override fun isExecuted() = callback != null
        override fun clone(): Call<TracksSearchResponse> = FakeCall()
        override fun execute(): Response<TracksSearchResponse> = error("Use enqueue")
        override fun request(): Request = Request.Builder().url("https://example.com").build()
        override fun timeout(): Timeout = Timeout.NONE
    }
}
