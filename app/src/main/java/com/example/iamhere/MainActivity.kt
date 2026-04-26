package com.example.iamhere

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.iamhere.service.MeshForegroundService
import com.example.iamhere.ui.screen.AppRoot
import com.example.iamhere.ui.theme.IAmHereTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundService(Intent(this, MeshForegroundService::class.java))
        setContent { IAmHereTheme { AppRoot() } }
    }
}
