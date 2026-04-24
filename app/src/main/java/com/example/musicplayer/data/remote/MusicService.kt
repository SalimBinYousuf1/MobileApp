package com.example.musicplayer.data.remote

import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicService {
    @GET("tracks/recommended")
    suspend fun forYou(): List<Track>

    @GET("tracks/search")
    suspend fun search(@Query("q") query: String): List<Track>

    @GET("playlists/trending")
    suspend fun trendingPlaylists(): List<Playlist>
}
