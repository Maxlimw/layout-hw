package com.praktikum.playlistmaker2

import com.praktikum.playlistmaker2.domain.impl.*
import com.praktikum.playlistmaker2.domain.model.*
import com.praktikum.playlistmaker2.domain.repository.*
import org.junit.Assert.*
import org.junit.Test

class DomainInteractorsTest {
    @Test
    fun historyDeduplicatesMovesToFrontAndKeepsTen() {
        val repository = MemoryHistory()
        val interactor = HistoryInteractorImpl(repository)
        (1L..12L).forEach { interactor.addTrack(track(it)) }
        val updated = track(5).copy(trackName = "Updated", previewUrl = "https://example.com/preview")
        interactor.addTrack(updated)
        assertEquals(updated, interactor.getTracks().first())
        assertEquals(listOf(5L, 12L, 11L, 10L, 9L, 8L, 7L, 6L, 4L, 3L), repository.tracks.map { it.trackId })
        interactor.clear()
        assertTrue(repository.tracks.isEmpty())
    }

    @Test
    fun searchTrimsQueryAndPropagatesCancellation() {
        var requested = ""
        var cancelled = false
        val repository = object : TracksRepository {
            override fun search(query: String, callback: (SearchResult) -> Unit): Cancellable {
                requested = query
                callback(SearchResult.Success(listOf(track(1))))
                return Cancellable { cancelled = true }
            }
        }
        var result: SearchResult? = null
        val interactor = SearchInteractorImpl(repository)
        val request = interactor.search("  Beatles  ") { result = it }
        assertEquals("Beatles", requested)
        assertEquals(SearchResult.Success(listOf(track(1))), result)
        request.cancel()
        assertTrue(cancelled)
        interactor.search("   ") { result = it }
        assertEquals("Beatles", requested)
        assertEquals(SearchResult.Success(emptyList()), result)
    }

    @Test
    fun settingsPersistThroughRepository() {
        var saved = ThemeSettings(false)
        val repository = object : SettingsRepository {
            override fun read() = saved
            override fun save(settings: ThemeSettings) { saved = settings }
        }
        SettingsInteractorImpl(repository).setDarkTheme(true)
        assertTrue(SettingsInteractorImpl(repository).getSettings().darkTheme)
    }

    @Test
    fun playerWaitsForPreparationAndResumesOrRestarts() {
        val repository = FakePlayer()
        val interactor = PlayerInteractorImpl(repository)
        var state: PlayerState? = null
        interactor.prepare("preview") { state = it }
        interactor.togglePlayback()
        assertEquals(0, repository.starts)
        repository.emit(PlayerState.READY)
        interactor.togglePlayback()
        assertEquals(PlayerState.PLAYING, state)
        interactor.togglePlayback()
        assertEquals(PlayerState.PAUSED, state)
        interactor.togglePlayback()
        assertEquals(2, repository.starts)
        repository.emit(PlayerState.COMPLETED)
        interactor.togglePlayback()
        assertEquals(3, repository.starts)
        interactor.pause()
        interactor.release()
        assertTrue(repository.released)
    }

    @Test
    fun missingPreviewDoesNotPreparePlayer() {
        val repository = FakePlayer()
        val interactor = PlayerInteractorImpl(repository)
        var state: PlayerState? = null
        interactor.prepare(null) { state = it }
        interactor.togglePlayback()
        assertEquals(PlayerState.UNAVAILABLE, state)
        assertEquals(0, repository.starts)
        assertNull(repository.listener)
    }

    @Test
    fun sharingDelegatesToRepository() {
        val actions = mutableListOf<String>()
        val repository = object : SharingRepository {
            override fun shareApp() { actions.add("share") }
            override fun contactSupport() { actions.add("support") }
            override fun openTerms() { actions.add("terms") }
        }
        SharingInteractorImpl(repository).apply { shareApp(); contactSupport(); openTerms() }
        assertEquals(listOf("share", "support", "terms"), actions)
    }

    private fun track(id: Long) = Track("Track $id", "Artist", 123000L, null, id)

    private class MemoryHistory : HistoryRepository {
        var tracks = emptyList<Track>()
        override fun read() = tracks
        override fun save(tracks: List<Track>) { this.tracks = tracks }
        override fun clear() { tracks = emptyList() }
    }

    private class FakePlayer : PlayerRepository {
        var listener: ((PlayerState) -> Unit)? = null
        var starts = 0
        var released = false
        fun emit(state: PlayerState) { listener?.invoke(state) }
        override fun prepare(url: String, onStateChanged: (PlayerState) -> Unit) { listener = onStateChanged }
        override fun start() { starts++; emit(PlayerState.PLAYING) }
        override fun pause() { emit(PlayerState.PAUSED) }
        override fun positionMillis() = 1234
        override fun release() { released = true; listener = null }
    }
}
