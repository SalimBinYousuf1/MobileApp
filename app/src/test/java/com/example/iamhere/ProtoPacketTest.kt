package com.example.iamhere

import com.example.iamhere.proto.MeshPacket
import com.example.iamhere.proto.TextMessage
import com.google.protobuf.ByteString
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtoPacketTest {
    @Test
    fun meshPacket_roundTrip_preservesFields() {
        val packet = MeshPacket.newBuilder()
            .setPacketId("id-1")
            .setSenderPubKey("sender")
            .setTimestamp(100L)
            .setTtl(5)
            .setText(TextMessage.newBuilder().setEncryptedContent(ByteString.copyFromUtf8("abc")).setRecipientPubKey("rcp").build())
            .build()

        val parsed = MeshPacket.parseFrom(packet.toByteArray())
        assertEquals("id-1", parsed.packetId)
        assertEquals("sender", parsed.senderPubKey)
        assertEquals(5, parsed.ttl)
        assertEquals("rcp", parsed.text.recipientPubKey)
    }
}
