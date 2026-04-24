package com.example.musicplayer.domain.repository

import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun observeForYouFeed(): Flow<List<Track>>
    fun observeOfflineLibrary(): Flow<List<Track>>
    suspend fun searchEverywhere(query: String): List<Track>
    suspend fun fetchTrendingPlaylists(): List<Playlist>
    suspend fun downloadTrack(track: Track)
    suspend fun updatePreferences(preferences: UserPreferences)
    fun observePreferences(): Flow<UserPreferences>
}
