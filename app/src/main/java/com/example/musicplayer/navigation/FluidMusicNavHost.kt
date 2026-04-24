package com.example.musicplayer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicplayer.features.downloads.DownloadsScreen
import com.example.musicplayer.features.home.HomeScreen
import com.example.musicplayer.features.library.LibraryScreen
import com.example.musicplayer.features.player.NowPlayingScreen
import com.example.musicplayer.features.profile.ProfileScreen
import com.example.musicplayer.features.search.SearchScreen
import com.example.musicplayer.features.settings.SettingsScreen

enum class TopRoute(val route: String, val label: String, val emoji: String) {
    Home("home", "Home", "🏠"),
    Search("search", "Search", "🔎"),
    Library("library", "Library", "🎵"),
    Player("player", "Player", "▶️"),
    Settings("settings", "Settings", "⚙️")
}

@Composable
fun FluidMusicNavHost() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopRoute.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = { navController.navigate(tab.route) },
                        icon = { Icon(imageVector = androidx.compose.material.icons.Icons.Default.Home, contentDescription = tab.label) },
                        label = { Text("${tab.emoji} ${tab.label}") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopRoute.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TopRoute.Home.route) { HomeScreen() }
            composable(TopRoute.Search.route) { SearchScreen() }
            composable(TopRoute.Library.route) { LibraryScreen() }
            composable(TopRoute.Player.route) { NowPlayingScreen() }
            composable(TopRoute.Settings.route) { SettingsScreen() }
            composable("downloads") { DownloadsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
