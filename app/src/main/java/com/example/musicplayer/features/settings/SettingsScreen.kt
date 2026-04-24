package com.example.musicplayer.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var offlineMode by remember { mutableStateOf(false) }
    var dataSaver by remember { mutableStateOf(true) }
    var dynamicTheme by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pro Settings")
        SettingToggle("Offline-first mode", offlineMode) { offlineMode = it }
        SettingToggle("Data saver on mobile", dataSaver) { dataSaver = it }
        SettingToggle("Dynamic fluid theme", dynamicTheme) { dynamicTheme = it }
        Text("Also supports: parental filter, explicit blocker, sleep timer, automation intents")
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
