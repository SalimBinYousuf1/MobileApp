package com.example.iamhere.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean("auto_start", true)) {
            context.startForegroundService(Intent(context, MeshForegroundService::class.java))
        }
    }
}
