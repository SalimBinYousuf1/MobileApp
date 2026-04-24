package com.example.musicplayer.data.repository

import com.example.musicplayer.data.local.TrackDao
import com.example.musicplayer.data.local.TrackEntity
import com.example.musicplayer.data.remote.MusicService
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.domain.model.UserPreferences
import com.example.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DefaultMusicRepository(
    private val trackDao: TrackDao,
    private val musicService: MusicService
) : MusicRepository {

    private val preferences = MutableStateFlow(UserPreferences())

    override fun observeForYouFeed(): Flow<List<Track>> {
        return combine(
            trackDao.observeAll().map { list -> list.map { it.toTrack() } },
            preferences
        ) { offline, pref ->
            offline.sortedByDescending { it.lyricsAvailable || pref.highQualityStreaming }
        }.catch {
            emit(emptyList())
        }
    }

    override fun observeOfflineLibrary(): Flow<List<Track>> =
        trackDao.observeAll().map { list -> list.map { it.toTrack() } }

    override suspend fun searchEverywhere(query: String): List<Track> {
        val local = trackDao.search(query).map { it.toTrack() }
        val remote = runCatching { musicService.search(query) }.getOrDefault(emptyList())
        return (local + remote).distinctBy { it.id }
    }

    override suspend fun fetchTrendingPlaylists(): List<Playlist> =
        runCatching { musicService.trendingPlaylists() }.getOrDefault(emptyList())

    override suspend fun downloadTrack(track: Track) {
        trackDao.upsertAll(
            listOf(
                TrackEntity(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    artworkUrl = track.artworkUrl,
                    localPath = track.localPath ?: "/storage/emulated/0/Music/${track.id}.mp3"
                )
            )
        )
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        this.preferences.value = preferences
    }

    override fun observePreferences(): Flow<UserPreferences> = preferences
}

private fun TrackEntity.toTrack(): Track = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    artworkUrl = artworkUrl,
    localPath = localPath,
    lyricsAvailable = true
)
