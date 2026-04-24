package com.example.musicplayer.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState

@Composable
fun ProfileScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Profile & Social")
        Text("Favorite tracks: ${AppState.favoriteTrackIds.size}")
        Text("Playlists: ${AppState.playlists.size}")
        Text("Downloaded: ${AppState.downloaded.size}")
        Text("Queue history: ${AppState.queue.size}")
        Text("Features: collaborative playlists, shared queue, listening stats")
    }
}
