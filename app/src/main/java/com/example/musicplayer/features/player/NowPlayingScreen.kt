package com.example.musicplayer.features.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun NowPlayingScreen() {
    val progress = remember { mutableFloatStateOf(0.32f) }
    val bassBoost = remember { mutableFloatStateOf(0.55f) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Now Playing", style = MaterialTheme.typography.headlineSmall)
        Text("Track: Night Drive Skyline")
        Text("Artist: Nova Pulse")
        LinearProgressIndicator(progress = { progress.floatValue }, modifier = Modifier.fillMaxWidth())
        Slider(value = progress.floatValue, onValueChange = { progress.floatValue = it })
        Text("EQ Bass Boost")
        Slider(value = bassBoost.floatValue, onValueChange = { bassBoost.floatValue = it })
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Lyrics + Translation + Karaoke Mode") }
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Enable crossfade + gapless + replaygain") }
    }
}
