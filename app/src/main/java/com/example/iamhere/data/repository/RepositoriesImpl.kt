package com.example.iamhere.data.repository

import com.example.iamhere.data.local.ContactDao
import com.example.iamhere.data.local.ContactEntity
import com.example.iamhere.data.local.MessageDao
import com.example.iamhere.data.network.MeshEngine
import com.example.iamhere.domain.model.Contact
import com.example.iamhere.domain.model.Message
import com.example.iamhere.domain.repository.ContactRepository
import com.example.iamhere.domain.repository.MessageRepository
import com.example.iamhere.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(private val dao: MessageDao, private val meshEngine: MeshEngine) : MessageRepository {
    override fun getAllMessages(): Flow<List<Message>> = dao.getAllMessages().map { list -> list.map { Message(it.id, it.senderId, it.content, it.timestamp, it.isRead, it.isVerified) } }
    override fun getUnreadCount(): Flow<Int> = dao.unreadCount()
    override suspend fun sendMessage(text: String, recipient: String) = meshEngine.sendMessage(text, recipient)
    override suspend fun markAsRead(id: Long) = dao.markAsRead(id)
}

@Singleton
class ContactRepositoryImpl @Inject constructor(private val dao: ContactDao) : ContactRepository {
    override fun contacts(): Flow<List<Contact>> = dao.getAll().map { it.map { c -> Contact(c.pubKey, c.alias, c.qrCodeData, c.isTrusted) } }
    override suspend fun addOrUpdate(contact: Contact) = dao.insert(ContactEntity(contact.pubKey, contact.alias, contact.qrCodeData, contact.isTrusted))
    override suspend fun get(pubKey: String): Contact? = dao.getByPubKey(pubKey)?.let { Contact(it.pubKey, it.alias, it.qrCodeData, it.isTrusted) }
}

@Singleton
class NetworkRepositoryImpl @Inject constructor(meshEngine: MeshEngine) : NetworkRepository {
    override val networkStatus: Flow<String> = meshEngine.networkStatus
}
