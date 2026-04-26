package com.example.iamhere.data.repository

import com.example.iamhere.data.local.ContactDao
import com.example.iamhere.data.local.ContactEntity
import com.example.iamhere.data.local.MessageDao
import com.example.iamhere.data.network.MeshEngine
import com.example.iamhere.domain.model.Contact
import com.example.iamhere.domain.model.DeliveryState
import com.example.iamhere.domain.model.Message
import com.example.iamhere.domain.repository.ContactRepository
import com.example.iamhere.domain.repository.MessageRepository
import com.example.iamhere.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao,
    private val meshEngine: MeshEngine
) : MessageRepository {
    override fun getAllMessages(): Flow<List<Message>> = dao.getAllMessages().map { it.map(::toDomain) }
    override fun getMessagesByThread(threadId: String): Flow<List<Message>> = dao.getAllByThread(threadId).map { it.map(::toDomain) }
    override fun getUnreadCount(): Flow<Int> = dao.unreadCount()
    override suspend fun sendMessage(text: String, recipient: String) = meshEngine.sendMessage(text, recipient)
    override suspend fun markAsRead(id: Long) = dao.markAsRead(id)
    override suspend fun clearAll() = dao.clear()

    private fun toDomain(e: com.example.iamhere.data.local.MessageEntity): Message = Message(
        id = e.id,
        senderId = e.senderId,
        recipientId = e.recipientId,
        content = e.content,
        timestamp = e.timestamp,
        isRead = e.isRead,
        isVerified = e.isVerified,
        deliveryState = DeliveryState.valueOf(e.deliveryState)
    )
}

@Singleton
class ContactRepositoryImpl @Inject constructor(private val dao: ContactDao) : ContactRepository {
    override fun contacts(): Flow<List<Contact>> = dao.getAll().map { it.map { c -> Contact(c.pubKey, c.alias, c.qrCodeData, c.isTrusted) } }
    override suspend fun addOrUpdate(contact: Contact) = dao.insert(ContactEntity(contact.pubKey, contact.alias, contact.qrCodeData, contact.isTrusted))
    override suspend fun get(pubKey: String): Contact? = dao.getByPubKey(pubKey)?.let { Contact(it.pubKey, it.alias, it.qrCodeData, it.isTrusted) }
    override suspend fun clear() = dao.clear()
}

@Singleton
class NetworkRepositoryImpl @Inject constructor(private val meshEngine: MeshEngine) : NetworkRepository {
    override val networkStatus: Flow<String> = meshEngine.networkStatus
    override val myPublicKey: String = meshEngine.myPublicKey
    override suspend fun simulatePeerFoundForDebug() = meshEngine.simulatePeerFoundForDebug()
    override suspend fun resetKeys() = meshEngine.resetKeys()
}
