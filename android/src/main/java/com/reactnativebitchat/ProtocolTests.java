package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProtocolTests {
    @Test
    public void testPacketEncodingDecoding() {
        BitchatPacket packet = new BitchatPacket();
        packet.version = 1;
        packet.type = 0;
        packet.senderID = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        packet.recipientID = new byte[]{0, 0, 0, 0, 0, 0, 0, 0}; // Broadcast
        packet.timestamp = System.currentTimeMillis();
        packet.payload = new byte[]{9, 10};
        packet.ttl = 5;

        byte[] encoded = Protocol.encodePacket(packet);
        BitchatPacket decoded = Protocol.decodePacket(encoded);

        assertNotNull(decoded);
        assertEquals(packet.version, decoded.version);
        assertArrayEquals(packet.senderID, decoded.senderID);
        assertArrayEquals(packet.recipientID, decoded.recipientID);
        assertEquals(packet.timestamp, decoded.timestamp, 0);
        assertArrayEquals(packet.payload, decoded.payload);
        assertEquals(packet.ttl - 1, decoded.ttl); // TTL decremented
    }

    @Test
    public void testBroadcastPacket() {
        BitchatPacket packet = new BitchatPacket();
        packet.recipientID = new byte[8]; // All zeros for broadcast
        byte[] encoded = Protocol.encodePacket(packet);
        BitchatPacket decoded = Protocol.decodePacket(encoded);
        assertArrayEquals(new byte[8], decoded.recipientID);
    }

    @Test
    public void testPacketWithSignature() {
        BitchatPacket packet = new BitchatPacket();
        packet.signature = new byte[64];
        java.util.Arrays.fill(packet.signature, (byte) 1);
        byte[] encoded = Protocol.encodePacket(packet);
        BitchatPacket decoded = Protocol.decodePacket(encoded);
        assertArrayEquals(packet.signature, decoded.signature);
    }

    @Test
    public void testInvalidInput() {
        assertNull(Protocol.decodePacket(new byte[10])); // Too short
        assertNull(Protocol.decodePacket(null));
        BitchatPacket packet = new BitchatPacket();
        packet.version = 2; // Invalid version
        byte[] encoded = Protocol.encodePacket(packet);
        assertNull(Protocol.decodePacket(encoded));
    }
}