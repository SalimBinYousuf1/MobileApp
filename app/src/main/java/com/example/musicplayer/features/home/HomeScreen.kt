package com.example.musicplayer.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    val highlights = listOf(
        "AI DJ transitions", "Spatial audio", "Offline smart sync", "Live lyrics", "Mood radio", "Party session"
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("FluidMusic", style = MaterialTheme.typography.headlineMedium)
            Text("iPhone-like fluid UX on Android with pro controls")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                highlights.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
        }
        items((1..8).map { "For You Mix #$it" }) { mix ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(mix, style = MaterialTheme.typography.titleMedium)
                    Text("Adaptive sequencing • 320kbps offline cache • Cross-device handoff")
                }
            }
        }
    }
}
