package com.example.iamhere.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "packets", indices = [Index(value = ["timestamp"])])
data class PacketEntity(
    @PrimaryKey val packetId: String,
    val rawBytes: ByteArray,
    val ttl: Int,
    val timestamp: Long
)

@Entity(tableName = "messages", indices = [Index(value = ["senderId", "timestamp"])])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isVerified: Boolean,
    val deliveryState: String
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val pubKey: String,
    val alias: String,
    val qrCodeData: String,
    val isTrusted: Boolean
)
