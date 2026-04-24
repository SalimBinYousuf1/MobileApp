package com.example.musicplayer.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibraryScreen() {
    val sections = listOf(
        "Downloaded • 1248 tracks",
        "Playlists • 36",
        "Liked Songs • 882",
        "Hi-Res FLAC • 212",
        "Recently Added • 75"
    )
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(sections.size) { i ->
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(sections[i])
                    Text("Smart filters, folder browsing, duplicate cleaner, auto-tag fix")
                }
            }
        }
    }
}
