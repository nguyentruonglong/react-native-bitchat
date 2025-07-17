package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTests {
    @Test
    public void testMessageEncodingDecoding() {
        BitchatMessage msg = new BitchatMessage();
        msg.id = "123e4567-e89b-12d3-a456-426614174000";
        msg.sender = "user";
        msg.content = "Hello";
        msg.timestamp = System.currentTimeMillis();
        byte[] encoded = Protocol.encodeMessage(msg);
        BitchatMessage decoded = Protocol.decodeMessage(encoded);
        assertEquals(msg.content, decoded.content);
    }

    @Test
    public void testRoomMessage() {
        BitchatMessage msg = new BitchatMessage();
        msg.channel = "#room";
        byte[] encoded = Protocol.encodeMessage(msg);
        BitchatMessage decoded = Protocol.decodeMessage(encoded);
        assertEquals(msg.channel, decoded.channel);
    }

    @Test
    public void testEncryptedRoomMessage() {
        BitchatMessage msg = new BitchatMessage();
        msg.isEncrypted = true;
        msg.channel = "#secure";
        byte[] encoded = Protocol.encodeMessage(msg);
        BitchatMessage decoded = Protocol.decodeMessage(encoded);
        assertTrue(decoded.isEncrypted);
    }

    @Test
    public void testPrivateMessage() {
        BitchatMessage msg = new BitchatMessage();
        msg.isPrivate = true;
        msg.recipientNickname = "friend";
        byte[] encoded = Protocol.encodeMessage(msg);
        BitchatMessage decoded = Protocol.decodeMessage(encoded);
        assertTrue(decoded.isPrivate);
    }

    @Test
    public void testRelayMessage() {
        BitchatMessage msg = new BitchatMessage();
        msg.isRelay = true;
        msg.originalSender = "original";
        byte[] encoded = Protocol.encodeMessage(msg);
        BitchatMessage decoded = Protocol.decodeMessage(encoded);
        assertTrue(decoded.isRelay);
    }

    @Test
    public void testEmptyAndLongContent() {
        BitchatMessage emptyMsg = new BitchatMessage();
        emptyMsg.content = "";
        byte[] emptyEncoded = Protocol.encodeMessage(emptyMsg);
        BitchatMessage emptyDecoded = Protocol.decodeMessage(emptyEncoded);
        assertEquals("", emptyDecoded.content);

        String longContent = new String(new char[1000]).replace('\0', 'a');
        BitchatMessage longMsg = new BitchatMessage();
        longMsg.content = longContent;
        byte[] longEncoded = Protocol.encodeMessage(longMsg);
        BitchatMessage longDecoded = Protocol.decodeMessage(longEncoded);
        assertEquals(longContent, longDecoded.content);
    }
}