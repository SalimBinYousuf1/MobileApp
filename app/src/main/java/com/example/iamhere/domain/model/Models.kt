package com.example.iamhere.domain.model

data class Message(val id: Long, val senderId: String, val content: String, val timestamp: Long, val isRead: Boolean, val isVerified: Boolean)
data class Contact(val pubKey: String, val alias: String, val qrCodeData: String, val isTrusted: Boolean)
