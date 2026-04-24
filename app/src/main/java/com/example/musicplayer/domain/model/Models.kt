package com.example.musicplayer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkUrl: String? = null,
    val streamUrl: String? = null,
    val localPath: String? = null,
    val bitrateKbps: Int = 320,
    val explicit: Boolean = false,
    val lyricsAvailable: Boolean = false
)

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val tracks: List<Track>,
    val collaborative: Boolean = false,
    val smartRules: List<String> = emptyList()
)

@Serializable
data class UserPreferences(
    val highQualityStreaming: Boolean = true,
    val normalizeVolume: Boolean = true,
    val crossfadeSeconds: Int = 4,
    val sleepTimerMinutes: Int? = null,
    val autoDownloadWifiOnly: Boolean = true,
    val dynamicTheme: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val language: String = "en"
)
