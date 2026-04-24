package com.example.musicplayer.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.musicplayer.domain.model.Track

object AppState {
    val onlineCatalog = mutableStateListOf(
        Track("1", "Skyline Pulse", "Nova", "Neon Nights", 230_000, streamUrl = "https://example.com/1.mp3", lyricsAvailable = true),
        Track("2", "Deep Focus", "Lofty", "Work Session", 182_000, streamUrl = "https://example.com/2.mp3"),
        Track("3", "Rain Code", "Byte Beat", "Hack Flow", 201_000, streamUrl = "https://example.com/3.mp3", explicit = true),
        Track("4", "Morning Acoustic", "Lina", "Sunrise", 189_000, streamUrl = "https://example.com/4.mp3")
    )

    val downloaded = mutableStateListOf<Track>()
    val queue = mutableStateListOf<Track>()
    val favoriteTrackIds = mutableStateListOf<String>()
    val playlists = mutableStateMapOf<String, MutableList<Track>>()

    var currentTrack by mutableStateOf<Track?>(null)
    var isPlaying by mutableStateOf(false)
    var offlineMode by mutableStateOf(false)
    var shuffleMode by mutableStateOf(false)
    var repeatMode by mutableStateOf("Off")
    var sleepTimerMinutes by mutableIntStateOf(0)

    init {
        playlists["Favorites Mix"] = mutableListOf()
        playlists["Workout"] = mutableListOf()
    }

    fun play(track: Track) {
        currentTrack = track
        if (queue.none { it.id == track.id }) queue.add(track)
        isPlaying = true
    }

    fun togglePlayPause() {
        isPlaying = !isPlaying
    }

    fun nextTrack() {
        if (queue.isEmpty()) return
        val currentIndex = queue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
        currentTrack = queue.getOrElse((currentIndex + 1) % queue.size) { queue.first() }
        isPlaying = true
    }

    fun previousTrack() {
        if (queue.isEmpty()) return
        val currentIndex = queue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
        val previous = if (currentIndex - 1 < 0) queue.last() else queue[currentIndex - 1]
        currentTrack = previous
        isPlaying = true
    }

    fun download(track: Track) {
        if (downloaded.none { it.id == track.id }) {
            downloaded.add(track.copy(localPath = "/storage/emulated/0/Music/${track.id}.mp3"))
        }
    }

    fun toggleFavorite(track: Track) {
        if (favoriteTrackIds.contains(track.id)) favoriteTrackIds.remove(track.id) else favoriteTrackIds.add(track.id)
    }

    fun addToPlaylist(playlistName: String, track: Track) {
        val playlist = playlists.getOrPut(playlistName) { mutableListOf() }
        if (playlist.none { it.id == track.id }) playlist.add(track)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerMinutes = minutes
    }

    fun visibleTracks(): List<Track> = if (offlineMode) downloaded else onlineCatalog
}
