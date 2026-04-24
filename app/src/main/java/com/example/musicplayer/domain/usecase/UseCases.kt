package com.example.musicplayer.domain.usecase

import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

class GetForYouFeedUseCase(private val repository: MusicRepository) {
    operator fun invoke(): Flow<List<Track>> = repository.observeForYouFeed()
}

class SearchTracksUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(query: String): List<Track> = repository.searchEverywhere(query)
}
