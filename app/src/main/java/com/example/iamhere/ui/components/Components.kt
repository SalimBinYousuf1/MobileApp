package com.example.iamhere.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NetworkStatusBanner(status: String) {
    val bg = when {
        status.startsWith("Connected") -> Color(0xFF2E7D32)
        status == "Searching" -> Color(0xFFF9A825)
        else -> Color.Gray
    }
    Text(status, modifier = Modifier.fillMaxWidth().background(bg).padding(8.dp), color = Color.White)
}

@Composable
fun MessageBubble(content: String, isMe: Boolean, verified: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
        AnimatedVisibility(visible = true) {
            Column(
                modifier = Modifier
                    .padding(6.dp)
                    .background(if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Text(content)
                if (verified) Text("Verified", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable fun QRCodeGenerator(data: String) { Text("QR: $data") }
@Composable fun QRScanner(onRead: (String) -> Unit) { onRead("sample_pubkey") }
