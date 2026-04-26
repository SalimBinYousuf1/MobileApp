package com.example.iamhere.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(packet: PacketEntity)

    @Query("SELECT packetId FROM packets")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM packets WHERE timestamp < :olderThan")
    suspend fun deleteOld(olderThan: Long)
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE senderId = :threadId ORDER BY timestamp ASC")
    fun getAllByThread(threadId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    fun unreadCount(): Flow<Int>

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM messages")
    suspend fun clear()
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("SELECT * FROM contacts")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE pubKey = :pubKey LIMIT 1")
    suspend fun getByPubKey(pubKey: String): ContactEntity?

    @Query("DELETE FROM contacts")
    suspend fun clear()
}
