package com.example.iamhere.data.network

import android.content.Context
import android.os.PowerManager
import com.example.iamhere.data.local.MessageDao
import com.example.iamhere.data.local.MessageEntity
import com.example.iamhere.data.local.PacketDao
import com.example.iamhere.data.local.PacketEntity
import com.example.iamhere.data.security.CryptoManager
import com.example.iamhere.proto.KeyExchange
import com.example.iamhere.proto.MeshPacket
import com.example.iamhere.proto.SyncRequest
import com.example.iamhere.proto.SyncResponse
import com.example.iamhere.proto.TextMessage
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeshEngine @Inject constructor(
    private val context: Context,
    private val packetDao: PacketDao,
    private val messageDao: MessageDao,
    private val cryptoManager: CryptoManager
) {
    private val connections by lazy { Nearby.getConnectionsClient(context) }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val myKeys = cryptoManager.generateKeyPair()
    val myPublicKey: String = Base64.getEncoder().encodeToString(myKeys.first)

    private val _networkStatus = MutableStateFlow("Offline")
    val networkStatus: StateFlow<String> = _networkStatus
    val connectedPeers = ConcurrentHashMap<String, String>()

    private val serviceId = "com.example.iamhere.mesh"

    fun start() {
        _networkStatus.value = "Searching"
        startAdvertising(); startDiscovery(); startSyncLoop()
    }

    private fun startAdvertising() {
        connections.startAdvertising(UUID.randomUUID().toString(), serviceId, lifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    private fun startDiscovery() {
        connections.startDiscovery(serviceId, endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connections.requestConnection(UUID.randomUUID().toString(), endpointId, lifecycleCallback)
        }
        override fun onEndpointLost(endpointId: String) { connectedPeers.remove(endpointId) }
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connections.acceptConnection(endpointId, payloadCallback)
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                performHandshake(endpointId)
                _networkStatus.value = "Connected to ${connectedPeers.size + 1} peers"
            }
        }
        override fun onDisconnected(endpointId: String) { connectedPeers.remove(endpointId) }
    }

    private fun performHandshake(endpointId: String) {
        val signature = cryptoManager.sign(myKeys.first, myKeys.second)
        val packet = MeshPacket.newBuilder()
            .setPacketId(UUID.randomUUID().toString())
            .setSenderPubKey(myPublicKey)
            .setTimestamp(System.currentTimeMillis())
            .setTtl(5)
            .setKeyExchange(KeyExchange.newBuilder().setPublicKey(com.google.protobuf.ByteString.copyFrom(myKeys.first)).setSignature(com.google.protobuf.ByteString.copyFrom(signature)).build())
            .build()
        connections.sendPayload(endpointId, Payload.fromBytes(packet.toByteArray()))
    }

    fun sendMessage(text: String, recipientKey: String) {
        scope.launch {
            val encrypted = cryptoManager.seal(text, if (recipientKey.isBlank()) myPublicKey else recipientKey)
            val packet = MeshPacket.newBuilder().setPacketId(UUID.randomUUID().toString())
                .setSenderPubKey(myPublicKey).setTimestamp(System.currentTimeMillis()).setTtl(5)
                .setText(TextMessage.newBuilder().setEncryptedContent(com.google.protobuf.ByteString.copyFrom(encrypted)).setRecipientPubKey(recipientKey).build())
                .build()
            packetDao.insert(PacketEntity(packet.packetId, packet.toByteArray(), packet.ttl, packet.timestamp))
            if (recipientKey == myPublicKey) processPacket(packet.toByteArray(), "self")
            broadcastPacket(packet, null)
        }
    }

    private fun broadcastPacket(packet: MeshPacket, except: String?) {
        connectedPeers.keys.filter { it != except }.forEach {
            connections.sendPayload(it, Payload.fromBytes(packet.toByteArray()))
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { scope.launch { processPacket(it, endpointId) } }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private suspend fun processPacket(bytes: ByteArray, sourceEndpointId: String) {
        val packet = runCatching { MeshPacket.parseFrom(bytes) }.getOrNull() ?: return
        if (packet.packetId in packetDao.getAllIds()) return
        if (packet.ttl <= 0) return
        packetDao.insert(PacketEntity(packet.packetId, bytes, packet.ttl, packet.timestamp))

        when (packet.payloadTypeCase) {
            MeshPacket.PayloadTypeCase.KEY_EXCHANGE -> {
                val key = Base64.getEncoder().encodeToString(packet.keyExchange.publicKey.toByteArray())
                if (cryptoManager.verify(packet.keyExchange.publicKey.toByteArray(), packet.keyExchange.signature.toByteArray(), packet.senderPubKey)) {
                    connectedPeers[sourceEndpointId] = key
                }
            }
            MeshPacket.PayloadTypeCase.TEXT -> {
                val recipient = packet.text.recipientPubKey
                if (recipient.isBlank() || recipient == myPublicKey) {
                    val plain = cryptoManager.open(packet.text.encryptedContent.toByteArray(), myKeys.second, myKeys.first)
                    plain?.let {
                        messageDao.insert(MessageEntity(senderId = packet.senderPubKey, content = it, timestamp = packet.timestamp, isRead = false, isVerified = true))
                    }
                }
            }
            MeshPacket.PayloadTypeCase.SYNC_REQ -> respondSync(sourceEndpointId, packet)
            MeshPacket.PayloadTypeCase.SYNC_RES -> packet.syncRes.missingPacketsList.forEach { processPacket(it.toByteArray(), sourceEndpointId) }
            else -> {}
        }
        val forwarded = packet.toBuilder().setTtl(packet.ttl - 1).build()
        if (forwarded.ttl > 0) broadcastPacket(forwarded, sourceEndpointId)
    }

    private suspend fun respondSync(endpointId: String, requestPacket: MeshPacket) {
        val mine = packetDao.getAllIds().toSet()
        val req = requestPacket.syncReq.knownPacketIdsList.toSet()
        val missing = mine.minus(req)
        val resp = SyncResponse.newBuilder()
        missing.forEach { id -> /* minimal implementation intentionally omits payload retrieval */ }
        val packet = MeshPacket.newBuilder().setPacketId(UUID.randomUUID().toString()).setSenderPubKey(myPublicKey)
            .setTimestamp(System.currentTimeMillis()).setTtl(5).setSyncRes(resp.build()).build()
        connections.sendPayload(endpointId, Payload.fromBytes(packet.toByteArray()))
    }

    private fun startSyncLoop() {
        scope.launch {
            while (true) {
                delay(if (isScreenOn()) 60_000 else 30_000)
                val req = MeshPacket.newBuilder().setPacketId(UUID.randomUUID().toString()).setSenderPubKey(myPublicKey)
                    .setTimestamp(System.currentTimeMillis()).setTtl(5)
                    .setSyncReq(SyncRequest.newBuilder().addAllKnownPacketIds(packetDao.getAllIds()).build()).build()
                broadcastPacket(req, null)
            }
        }
    }

    private fun isScreenOn(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }
}
