package com.example.musicplayer.features.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState

@Composable
fun DownloadsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Smart Downloads")
        LinearProgressIndicator(progress = { if (AppState.onlineCatalog.isEmpty()) 0f else AppState.downloaded.size.toFloat() / AppState.onlineCatalog.size })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { AppState.offlineMode = true }) { Text("Go Offline") }
            Button(onClick = { AppState.downloaded.clear() }) { Text("Clear Downloads") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AppState.downloaded, key = { it.id }) { track ->
                Text("• ${track.title} (${track.localPath})")
            }
        }
    }
}
