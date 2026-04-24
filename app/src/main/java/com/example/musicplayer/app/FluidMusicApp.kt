package com.example.musicplayer.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.musicplayer.navigation.FluidMusicNavHost

private val DarkPalette = darkColorScheme()
private val LightPalette = lightColorScheme()

@Composable
fun FluidMusicApp() {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkPalette else LightPalette
    ) {
        FluidMusicNavHost()
    }
}
