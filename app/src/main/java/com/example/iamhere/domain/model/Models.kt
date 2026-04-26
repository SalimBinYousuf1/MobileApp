package com.example.iamhere.domain.model

enum class DeliveryState { QUEUED, SENT, DELIVERED }

data class Message(
    val id: Long,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isVerified: Boolean,
    val deliveryState: DeliveryState
)

data class Contact(val pubKey: String, val alias: String, val qrCodeData: String, val isTrusted: Boolean)
