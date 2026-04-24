package com.example.musicplayer.features.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState

@Composable
fun NowPlayingScreen() {
    val progress = remember { mutableFloatStateOf(0.12f) }
    val bassBoost = remember { mutableFloatStateOf(0.55f) }
    val current = AppState.currentTrack

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Now Playing", style = MaterialTheme.typography.headlineSmall)
        Text("Track: ${current?.title ?: "No track selected"}")
        Text("Artist: ${current?.artist ?: "-"}")
        LinearProgressIndicator(progress = { progress.floatValue }, modifier = Modifier.fillMaxWidth())
        Slider(value = progress.floatValue, onValueChange = { progress.floatValue = it })
        Text("EQ Bass Boost")
        Slider(value = bassBoost.floatValue, onValueChange = { bassBoost.floatValue = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { AppState.previousTrack() }) { Text("Prev") }
            Button(onClick = { AppState.togglePlayPause() }) { Text(if (AppState.isPlaying) "Pause" else "Play") }
            Button(onClick = { AppState.nextTrack() }) { Text("Next") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { current?.let(AppState::download) }) { Text("Save Offline") }
            Button(onClick = { current?.let(AppState::toggleFavorite) }) { Text("Like") }
        }
        Text("Queue: ${AppState.queue.size} • Repeat: ${AppState.repeatMode} • Shuffle: ${AppState.shuffleMode}")
    }
}
