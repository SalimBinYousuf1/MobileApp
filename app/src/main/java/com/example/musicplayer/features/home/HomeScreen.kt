package com.example.musicplayer.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState
import com.example.musicplayer.domain.model.Track

@Composable
fun HomeScreen() {
    val tracks = AppState.visibleTracks()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("FluidMusic", style = MaterialTheme.typography.headlineMedium)
            Text("Pro UX: streaming, offline, playlists, favorites, queue")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { AppState.offlineMode = false }, label = { Text("Online") })
                AssistChip(onClick = { AppState.offlineMode = true }, label = { Text("Offline") })
                AssistChip(onClick = { AppState.shuffleMode = !AppState.shuffleMode }, label = { Text(if (AppState.shuffleMode) "Shuffle ON" else "Shuffle OFF") })
            }
        }
        items(tracks, key = Track::id) { track ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(track.title, style = MaterialTheme.typography.titleMedium)
                    Text("${track.artist} • ${track.album}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { AppState.play(track) }) { Text("Play") }
                        Button(onClick = { AppState.download(track) }) { Text("Download") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { AppState.toggleFavorite(track) }) {
                            Text(if (AppState.favoriteTrackIds.contains(track.id)) "Unfavorite" else "Favorite")
                        }
                        Button(onClick = { AppState.addToPlaylist("Favorites Mix", track) }) { Text("Add Playlist") }
                    }
                }
            }
        }
    }
}
