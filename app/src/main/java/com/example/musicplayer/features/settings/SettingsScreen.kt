package com.example.musicplayer.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicplayer.app.AppState

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pro Settings")
        SettingToggle("Offline-first mode", AppState.offlineMode) { AppState.offlineMode = it }
        SettingToggle("Shuffle mode", AppState.shuffleMode) { AppState.shuffleMode = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { AppState.repeatMode = "Off" }) { Text("Repeat Off") }
            Button(onClick = { AppState.repeatMode = "All" }) { Text("Repeat All") }
            Button(onClick = { AppState.repeatMode = "One" }) { Text("Repeat One") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { AppState.setSleepTimer(15) }) { Text("Sleep 15m") }
            Button(onClick = { AppState.setSleepTimer(30) }) { Text("Sleep 30m") }
            Button(onClick = { AppState.setSleepTimer(0) }) { Text("Sleep Off") }
        }
        Text("Sleep timer: ${AppState.sleepTimerMinutes} minutes")
        Text("Downloads: ${AppState.downloaded.size} • Favorites: ${AppState.favoriteTrackIds.size}")
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
