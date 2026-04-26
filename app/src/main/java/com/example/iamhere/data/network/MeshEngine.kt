package com.example.iamhere.data.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
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
import com.google.protobuf.ByteString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeshEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packetDao: PacketDao,
    private val messageDao: MessageDao,
    private val cryptoManager: CryptoManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionsClient by lazy { Nearby.getConnectionsClient(context) }
    private val serviceId = "com.example.iamhere.mesh"

    private val keys get() = cryptoManager.getOrCreateKeys()
    val myPublicKey: String get() = cryptoManager.toB64(keys.boxPublic)
    private val mySignPublicKey: String get() = cryptoManager.toB64(keys.signPublic)

    private val _networkStatus = MutableStateFlow("Offline")
    val networkStatus: StateFlow<String> = _networkStatus

    val connectedPeers = ConcurrentHashMap<String, String>()

    fun start() {
        if (!hasNearbyPermissions()) {
            _networkStatus.value = "Offline"
            return
        }
        startAdvertising()
        startDiscovery()
        startMaintenanceLoops()
        _networkStatus.value = "Searching"
    }

    suspend fun resetKeys() {
        cryptoManager.resetKeys()
        connectedPeers.clear()
    }

    suspend fun simulatePeerFoundForDebug() {
        connectedPeers["debug-endpoint"] = myPublicKey
        _networkStatus.value = "Connected to ${connectedPeers.size} peers"
    }

    fun sendMessage(text: String, recipientKey: String) {
        scope.launch {
            val resolvedRecipient = recipientKey.ifBlank { myPublicKey }
            val encrypted = cryptoManager.seal(text, resolvedRecipient)
            val packet = MeshPacket.newBuilder()
                .setPacketId(UUID.randomUUID().toString())
                .setSenderPubKey(mySignPublicKey)
                .setTimestamp(System.currentTimeMillis())
                .setTtl(5)
                .setText(
                    TextMessage.newBuilder()
                        .setEncryptedContent(ByteString.copyFrom(encrypted))
                        .setRecipientPubKey(resolvedRecipient)
                        .build()
                )
                .build()

            packetDao.insert(PacketEntity(packet.packetId, packet.toByteArray(), packet.ttl, packet.timestamp))
            messageDao.insert(
                MessageEntity(
                    senderId = myPublicKey,
                    recipientId = resolvedRecipient,
                    content = text,
                    timestamp = packet.timestamp,
                    isRead = true,
                    isVerified = true,
                    deliveryState = if (connectedPeers.isEmpty()) "QUEUED" else "SENT"
                )
            )

            if (resolvedRecipient == myPublicKey) processPacket(packet.toByteArray(), "self")
            broadcastPacket(packet, exceptEndpoint = null)
        }
    }

    private fun startAdvertising() {
        connectionsClient.startAdvertising(
            UUID.randomUUID().toString(),
            serviceId,
            lifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
    }

    private fun startDiscovery() {
        connectionsClient.startDiscovery(
            serviceId,
            discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        )
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(UUID.randomUUID().toString(), endpointId, lifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            connectedPeers.remove(endpointId)
            _networkStatus.value = if (connectedPeers.isEmpty()) "Searching" else "Connected to ${connectedPeers.size} peers"
        }
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                scope.launch { performHandshake(endpointId); sendSyncRequest(endpointId) }
                _networkStatus.value = "Connected to ${connectedPeers.size + 1} peers"
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedPeers.remove(endpointId)
            _networkStatus.value = if (connectedPeers.isEmpty()) "Searching" else "Connected to ${connectedPeers.size} peers"
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes -> scope.launch { processPacket(bytes, endpointId) } }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private suspend fun performHandshake(endpointId: String) {
        val handshakeBytes = keys.boxPublic + keys.signPublic
        val signature = cryptoManager.sign(handshakeBytes, keys.signSecret)
        val packet = MeshPacket.newBuilder()
            .setPacketId(UUID.randomUUID().toString())
            .setSenderPubKey(mySignPublicKey)
            .setTimestamp(System.currentTimeMillis())
            .setTtl(5)
            .setKeyExchange(
                KeyExchange.newBuilder()
                    .setPublicKey(ByteString.copyFrom(handshakeBytes))
                    .setSignature(ByteString.copyFrom(signature))
                    .build()
            )
            .build()
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(packet.toByteArray()))
    }

    private suspend fun processPacket(raw: ByteArray, sourceEndpoint: String) {
        val packet = runCatching { MeshPacket.parseFrom(raw) }.getOrNull() ?: return
        if (packetDao.exists(packet.packetId) || packet.ttl <= 0) return

        packetDao.insert(PacketEntity(packet.packetId, raw, packet.ttl, packet.timestamp))

        when (packet.payloadTypeCase) {
            MeshPacket.PayloadTypeCase.KEY_EXCHANGE -> handleHandshake(packet, sourceEndpoint)
            MeshPacket.PayloadTypeCase.TEXT -> handleText(packet)
            MeshPacket.PayloadTypeCase.SYNC_REQ -> handleSyncRequest(packet, sourceEndpoint)
            MeshPacket.PayloadTypeCase.SYNC_RES -> handleSyncResponse(packet, sourceEndpoint)
            else -> Unit
        }

        val forwarded = packet.toBuilder().setTtl(packet.ttl - 1).build()
        if (forwarded.ttl > 0) broadcastPacket(forwarded, sourceEndpoint)
    }

    private fun handleHandshake(packet: MeshPacket, endpointId: String) {
        val keyMaterial = packet.keyExchange.publicKey.toByteArray()
        if (keyMaterial.size < 64) return
        val peerBoxPublic = keyMaterial.copyOfRange(0, 32)
        val senderSignKey = packet.senderPubKey
        val verified = cryptoManager.verify(keyMaterial, packet.keyExchange.signature.toByteArray(), senderSignKey)
        if (verified) {
            connectedPeers[endpointId] = cryptoManager.toB64(peerBoxPublic)
            _networkStatus.value = "Connected to ${connectedPeers.size} peers"
        }
    }

    private suspend fun handleText(packet: MeshPacket) {
        val recipient = packet.text.recipientPubKey
        if (recipient != myPublicKey && recipient.isNotBlank()) return
        val plaintext = cryptoManager.open(packet.text.encryptedContent.toByteArray(), keys.boxPublic, keys.boxSecret) ?: return
        messageDao.insert(
            MessageEntity(
                senderId = packet.senderPubKey,
                recipientId = recipient,
                content = plaintext,
                timestamp = packet.timestamp,
                isRead = false,
                isVerified = true,
                deliveryState = "DELIVERED"
            )
        )
    }

    private suspend fun handleSyncRequest(packet: MeshPacket, endpointId: String) {
        val knownIds = packet.syncReq.knownPacketIdsList.toSet()
        val localIds = packetDao.getAllIds()
        val missingIds = localIds.filterNot { it in knownIds }
        val missingPackets = packetDao.getByIds(missingIds).mapNotNull { entity ->
            runCatching { MeshPacket.parseFrom(entity.rawBytes) }.getOrNull()
        }
        val response = MeshPacket.newBuilder()
            .setPacketId(UUID.randomUUID().toString())
            .setSenderPubKey(mySignPublicKey)
            .setTimestamp(System.currentTimeMillis())
            .setTtl(5)
            .setSyncRes(SyncResponse.newBuilder().addAllMissingPackets(missingPackets).build())
            .build()
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(response.toByteArray()))
    }

    private suspend fun handleSyncResponse(packet: MeshPacket, sourceEndpoint: String) {
        packet.syncRes.missingPacketsList.forEach { missing ->
            processPacket(missing.toByteArray(), sourceEndpoint)
        }
    }

    private suspend fun sendSyncRequest(endpointId: String? = null) {
        val req = MeshPacket.newBuilder()
            .setPacketId(UUID.randomUUID().toString())
            .setSenderPubKey(mySignPublicKey)
            .setTimestamp(System.currentTimeMillis())
            .setTtl(5)
            .setSyncReq(SyncRequest.newBuilder().addAllKnownPacketIds(packetDao.getAllIds()).build())
            .build()

        if (endpointId != null) {
            connectionsClient.sendPayload(endpointId, Payload.fromBytes(req.toByteArray()))
        } else {
            broadcastPacket(req, null)
        }
    }

    private fun broadcastPacket(packet: MeshPacket, exceptEndpoint: String?) {
        connectedPeers.keys.filter { it != exceptEndpoint }.forEach { endpoint ->
            connectionsClient.sendPayload(endpoint, Payload.fromBytes(packet.toByteArray()))
        }
    }

    private fun startMaintenanceLoops() {
        scope.launch {
            while (true) {
                delay(60_000)
                sendSyncRequest()
                packetDao.deleteOld(System.currentTimeMillis() - 86_400_000L)
            }
        }
        scope.launch {
            while (true) {
                val delayMs = if (isScreenOn()) 5_000L else 30_000L
                delay(delayMs)
                if (connectedPeers.isEmpty()) {
                    startAdvertising()
                    startDiscovery()
                }
            }
        }
    }

    private fun isScreenOn(): Boolean =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

    private fun hasNearbyPermissions(): Boolean {
        val required = mutableListOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            required += Manifest.permission.ACCESS_FINE_LOCATION
        }
        return required.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }
}
