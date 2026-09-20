package com.praktikum.playlistmaker2

import com.praktikum.playlistmaker2.data.dto.*
import com.praktikum.playlistmaker2.data.network.ItunesApi
import com.praktikum.playlistmaker2.data.repository.TracksRepositoryImpl
import com.praktikum.playlistmaker2.domain.model.SearchResult
import okhttp3.Request
import okio.Timeout
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class TracksRepositoryTest {
    @Test
    fun convertsNetworkDtoBeforeDeliveringSearchResult() {
        val call = FakeCall()
        val repository = repository(call)
        var result: SearchResult? = null
        repository.search("song") { result = it }
        call.respond(Response.success(TracksSearchResponse(1, listOf(
            TrackDto("Song", "Artist", 120000L, "cover", 17L, "Album", "1999", "Rock", "GBR", "preview")
        ))))
        val track = (result as SearchResult.Success).tracks.single()
        assertEquals(17L, track.trackId)
        assertEquals("preview", track.previewUrl)
        assertEquals("Album", track.collectionName)
        assertEquals(120000L, track.trackTimeMillis)
    }

    @Test
    fun cancellationStopsLateResponseAndFailureDelivery() {
        val call = FakeCall()
        var deliveries = 0
        val request = repository(call).search("song") { deliveries++ }
        request.cancel()
        call.respond(Response.success(TracksSearchResponse(0, emptyList())))
        call.fail()
        assertTrue(call.isCanceled)
        assertEquals(0, deliveries)
    }

    @Test
    fun transportFailureBecomesDomainError() {
        val call = FakeCall()
        var result: SearchResult? = null
        repository(call).search("song") { result = it }
        call.fail()
        assertEquals(SearchResult.Error, result)
    }

    private fun repository(call: FakeCall) = TracksRepositoryImpl(object : ItunesApi {
        override fun search(text: String): Call<TracksSearchResponse> = call
    })

    private class FakeCall : Call<TracksSearchResponse> {
        private lateinit var callback: Callback<TracksSearchResponse>
        private var cancelled = false
        fun respond(response: Response<TracksSearchResponse>) = callback.onResponse(this, response)
        fun fail() = callback.onFailure(this, IOException("offline"))
        override fun enqueue(callback: Callback<TracksSearchResponse>) { this.callback = callback }
        override fun cancel() { cancelled = true }
        override fun isCanceled() = cancelled
        override fun isExecuted() = ::callback.isInitialized
        override fun clone(): Call<TracksSearchResponse> = FakeCall()
        override fun execute(): Response<TracksSearchResponse> = error("Use enqueue")
        override fun request(): Request = Request.Builder().url("https://example.com").build()
        override fun timeout(): Timeout = Timeout.NONE
    }
}
