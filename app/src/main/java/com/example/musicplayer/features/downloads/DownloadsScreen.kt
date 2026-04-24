package com.example.musicplayer.features.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DownloadsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Smart Downloads")
        Text("Batch queue: 38 tracks • remaining 412MB")
        LinearProgressIndicator(progress = { 0.63f })
        Text("Wifi-only • Retry strategy • Integrity verification • Auto-clean")
    }
}
