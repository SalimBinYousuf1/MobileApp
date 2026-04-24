package com.example.musicplayer.features.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    val suggestions = listOf("Lo-fi focus", "Workout EDM", "Acoustic chill", "90s Hindi", "Podcast tech")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Universal Search", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search tracks, albums, artists, podcasts") }
        )
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Search online + local") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions.size) {
                Text("• ${suggestions[it]}")
            }
        }
    }
}
