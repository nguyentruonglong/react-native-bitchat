package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChannelTests {
    @Test
    public void testKeyDerivation() {
        Channel channel = new Channel();
        channel.createChannel("#test", "password", "creator");
        byte[] key = channel.channelKeys.get("#test");
        assertNotNull(key);
        assertEquals(32, key.length); // 32 bytes from PBKDF2
    }

    @Test
    public void testJoinUnprotectedChannel() {
        Channel channel = new Channel();
        channel.createChannel("#open", null, "creator");
        assertTrue(channel.joinChannel("#open", null, "peer"));
    }

    @Test
    public void testJoinProtectedChannel() {
        Channel channel = new Channel();
        channel.createChannel("#secure", "pass", "creator");
        assertTrue(channel.joinChannel("#secure", "pass", "peer"));
        assertFalse(channel.joinChannel("#secure", "wrong", "peer"));
    }

    @Test
    public void testEncryptionDecryption() {
        Channel channel = new Channel();
        channel.createChannel("#enc", "pass", "creator");
        channel.joinChannel("#enc", "pass", "peer");
        // Assume encryption/decryption logic in receiveMessage
        BitchatMessage msg = new BitchatMessage();
        msg.isEncrypted = true;
        msg.channel = "#enc";
        channel.receiveMessage(msg);
        // Verify system message or decrypted content (placeholder)
        assertTrue(channel.getSystemMessages().size() > 0);
    }

    @Test
    public void testPasswordManagement() {
        Channel channel = new Channel();
        channel.createChannel("#pass", "old", "creator");
        channel.setChannelPassword("#pass", "new", "creator");
        assertEquals("new", channel.channelPasswords.get("#pass"));
        channel.removeChannelPassword("#pass", "creator");
        assertNull(channel.channelPasswords.get("#pass"));
    }

    @Test
    public void testOwnershipTransfer() {
        Channel channel = new Channel();
        channel.createChannel("#own", "pass", "creator");
        channel.transferOwnership("#own", "newOwner", "creator");
        assertEquals("newOwner", channel.channelCreators.get("#own"));
    }

    @Test
    public void testCommands() {
        Channel channel = new Channel();
        channel.processCommand("/join #cmd", "peer");
        assertTrue(channel.joinedChannels.contains("#cmd"));
    }
}