package com.praktikum.playlistmaker2

import com.google.gson.Gson
import com.praktikum.playlistmaker2.data.dto.*
import com.praktikum.playlistmaker2.data.mapper.*
import com.praktikum.playlistmaker2.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class DtoMappingTest {
    @Test
    fun allTrackFieldsSurviveStorageRoundTrip() {
        val track = Track("Song", "Artist", 123000L, "cover", 9L, "Album", "2001-01-01", "Rock", "USA", "preview")
        val json = Gson().toJson(track.toDto())
        assertEquals(track, Gson().fromJson(json, TrackDto::class.java).toDomain())
    }

    @Test
    fun oldHistoryHasNullOptionalFields() {
        val dto = Gson().fromJson("""{"trackId":7,"trackName":"Old"}""", TrackDto::class.java)
        assertEquals(7L, dto.toDomain().trackId)
        assertNull(dto.toDomain().previewUrl)
        assertNull(dto.toDomain().collectionName)
    }

    @Test
    fun themeMappingPreservesBothValues() {
        listOf(false, true).forEach { assertEquals(ThemeSettings(it), ThemeSettings(it).toDto().toDomain()) }
    }
}
