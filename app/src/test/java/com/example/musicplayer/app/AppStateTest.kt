package com.example.musicplayer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppStateTest {

    @Before
    fun resetState() {
        AppState.downloaded.clear()
        AppState.queue.clear()
        AppState.favoriteTrackIds.clear()
        AppState.playlists.clear()
        AppState.playlists["Favorites Mix"] = mutableListOf()
        AppState.playlists["Workout"] = mutableListOf()
        AppState.currentTrack = null
        AppState.isPlaying = false
        AppState.offlineMode = false
        AppState.shuffleMode = false
        AppState.repeatMode = "Off"
        AppState.setSleepTimer(0)
    }

    @Test
    fun play_addsTrackToQueueAndMarksPlaying() {
        val track = AppState.onlineCatalog.first()
        AppState.play(track)

        assertEquals(track.id, AppState.currentTrack?.id)
        assertTrue(AppState.queue.any { it.id == track.id })
        assertTrue(AppState.isPlaying)
    }

    @Test
    fun download_andFavorite_workAsExpected() {
        val track = AppState.onlineCatalog.first()
        AppState.download(track)
        AppState.toggleFavorite(track)

        assertTrue(AppState.downloaded.any { it.id == track.id })
        assertTrue(AppState.favoriteTrackIds.contains(track.id))
    }

    @Test
    fun playlistAndSleepTimer_updatesPersist() {
        val track = AppState.onlineCatalog.first()
        AppState.addToPlaylist("Workout", track)
        AppState.setSleepTimer(30)

        assertEquals(1, AppState.playlists["Workout"]?.size)
        assertEquals(30, AppState.sleepTimerMinutes)
    }
}
