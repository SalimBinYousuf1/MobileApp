package com.example.iamhere.domain.repository

import com.example.iamhere.domain.model.Contact
import com.example.iamhere.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllMessages(): Flow<List<Message>>
    fun getUnreadCount(): Flow<Int>
    suspend fun sendMessage(text: String, recipient: String)
    suspend fun markAsRead(id: Long)
}

interface ContactRepository {
    fun contacts(): Flow<List<Contact>>
    suspend fun addOrUpdate(contact: Contact)
    suspend fun get(pubKey: String): Contact?
}

interface NetworkRepository {
    val networkStatus: Flow<String>
}
