package com.example.musicplayer.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState

@Composable
fun LibraryScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Offline Library (${AppState.downloaded.size})") }
        items(AppState.downloaded, key = { it.id }) { track ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(track.title)
                    Text("${track.artist} • local: ${track.localPath ?: "pending"}")
                }
            }
        }
        item { Text("Playlists") }
        items(AppState.playlists.keys.toList(), key = { it }) { name ->
            val count = AppState.playlists[name]?.size ?: 0
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(name)
                    Text("Tracks: $count")
                }
            }
        }
    }
}
