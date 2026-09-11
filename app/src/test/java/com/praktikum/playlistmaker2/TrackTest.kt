package com.praktikum.playlistmaker2

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class TrackTest {
    @Test
    fun durationDoesNotDependOnTimeZoneOrWrapAtAnHour() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kathmandu"))
            assertEquals("61:05", track().copy(trackTimeMillis = 3665000L).getFormattedTime())
            assertEquals("00:00", track().copy(trackTimeMillis = null).getFormattedTime())
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun replacesOnlyLastArtworkPathSegment() {
        assertEquals("https://example.com/album/512x512bb.jpg",
            track().copy(artworkUrl100 = "https://example.com/album/100x100bb.jpg").getCoverArtwork())
        assertNull(track().getCoverArtwork())
        assertNull(track().copy(artworkUrl100 = "").getCoverArtwork())
    }

    @Test
    fun readsOldHistoryWithoutNewFields() {
        val restored = Gson().fromJson("""{"trackId":1,"trackName":"Old track"}""", Track::class.java)
        assertNull(restored.collectionName)
        assertNull(restored.getReleaseYear())
    }

    @Test
    fun extractsYearAndHandlesMissingDate() {
        assertEquals("1965", track().copy(releaseDate = "1965-08-06T12:00:00Z").getReleaseYear())
        assertNull(track().copy(releaseDate = "").getReleaseYear())
        assertNull(track().copy(releaseDate = "bad date").getReleaseYear())
    }

    private fun track() = Track("Track", "Artist", 65000L, null, 1L)
}
