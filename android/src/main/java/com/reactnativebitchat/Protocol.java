package com.reactnativebitchat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Protocol {
    private static final int VERSION = 1;
    private static final int HEADER_SIZE = 29; // version(1) + type(1) + senderID(8) + recipientID(8) + timestamp(8) + ttl(1) + payloadLength(2)
    private static final byte FRAGMENT_START = 0x01;
    private static final byte FRAGMENT_CONTINUE = 0x02;
    private static final byte FRAGMENT_END = 0x03;
    private static final Map<String, byte[]> fragmentBuffer = new HashMap<>();

    public static byte[] encodePacket(BitchatPacket packet) {
        if (packet.ttl <= 0) packet.ttl = 1; // Ensure TTL is at least 1
        packet.ttl--; // Decrement TTL for routing

        int payloadLength = packet.payload != null ? packet.payload.length : 0;
        int signatureLength = packet.signature != null ? 64 : 0;
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payloadLength + signatureLength);

        buffer.put((byte) VERSION);
        if (payloadLength > 0) {
            if (payloadLength > 1024) { // Arbitrary max fragment size
                byte fragmentType = FRAGMENT_START;
                int offset = 0;
                while (offset < payloadLength) {
                    int chunkSize = Math.min(1024, payloadLength - offset);
                    byte[] chunk = new byte[chunkSize];
                    System.arraycopy(packet.payload, offset, chunk, 0, chunkSize);
                    BitchatPacket fragmentPacket = new BitchatPacket();
                    fragmentPacket.version = VERSION;
                    fragmentPacket.type = fragmentType;
                    fragmentPacket.senderID = packet.senderID;
                    fragmentPacket.recipientID = packet.recipientID;
                    fragmentPacket.timestamp = packet.timestamp;
                    fragmentPacket.ttl = packet.ttl;
                    fragmentPacket.payload = chunk;
                    fragmentPacket.signature = packet.signature;
                    buffer.put((byte) fragmentPacket.type);
                    buffer.put(padOrTruncate(fragmentPacket.senderID, 8));
                    buffer.put(padOrTruncate(fragmentPacket.recipientID, 8));
                    buffer.putLong(fragmentPacket.timestamp);
                    buffer.put((byte) fragmentPacket.ttl);
                    buffer.putShort((short) chunkSize);
                    buffer.put(chunk);
                    if (fragmentPacket.signature != null) buffer.put(fragmentPacket.signature);
                    offset += chunkSize;
                    fragmentType = FRAGMENT_CONTINUE;
                }
                buffer.put(FRAGMENT_END); // Mark end of fragments
            } else {
                buffer.put((byte) packet.type);
                buffer.put(padOrTruncate(packet.senderID, 8));
                buffer.put(padOrTruncate(packet.recipientID, 8));
                buffer.putLong(packet.timestamp);
                buffer.put((byte) packet.ttl);
                buffer.putShort((short) payloadLength);
                if (packet.payload != null) buffer.put(packet.payload);
                if (packet.signature != null) buffer.put(packet.signature);
            }
        } else {
            buffer.put((byte) packet.type);
            buffer.put(padOrTruncate(packet.senderID, 8));
            buffer.put(padOrTruncate(packet.recipientID, 8));
            buffer.putLong(packet.timestamp);
            buffer.put((byte) packet.ttl);
            buffer.putShort((short) 0);
            if (packet.signature != null) buffer.put(packet.signature);
        }

        return buffer.array();
    }

    public static BitchatPacket decodePacket(byte[] data) {
        if (data == null || data.length < HEADER_SIZE) return null;

        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte version = buffer.get();
        if (version != VERSION) return null; // Validate version

        BitchatPacket packet = new BitchatPacket();
        packet.version = version;
        packet.type = buffer.get();
        packet.senderID = new byte[8];
        buffer.get(packet.senderID);
        packet.recipientID = new byte[8];
        buffer.get(packet.recipientID);
        packet.timestamp = buffer.getLong();
        packet.ttl = buffer.get();
        short payloadLength = buffer.getShort();

        // Handle fragmentation
        String key = new String(packet.senderID) + "_" + packet.timestamp;
        if (packet.type == FRAGMENT_START || packet.type == FRAGMENT_CONTINUE || packet.type == FRAGMENT_END) {
            byte[] fragmentData = new byte[payloadLength];
            buffer.get(fragmentData);
            byte[] existing = fragmentBuffer.getOrDefault(key, new byte[0]);
            byte[] newData = new byte[existing.length + fragmentData.length];
            System.arraycopy(existing, 0, newData, 0, existing.length);
            System.arraycopy(fragmentData, 0, newData, existing.length, fragmentData.length);
            fragmentBuffer.put(key, newData);

            if (packet.type == FRAGMENT_END) {
                packet.payload = fragmentBuffer.remove(key);
                if (buffer.remaining() >= 64) {
                    packet.signature = new byte[64];
                    buffer.get(packet.signature);
                }
                return packet;
            }
            return null; // Wait for more fragments
        }

        if (payloadLength > 0) {
            packet.payload = new byte[payloadLength];
            buffer.get(packet.payload);
        }
        if (buffer.remaining() >= 64) {
            packet.signature = new byte[64];
            buffer.get(packet.signature);
        }

        return packet;
    }

    public static byte[] encodeMessage(BitchatMessage message) {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // Adjust size as needed
        byte[] contentBytes = (message.content != null ? message.content.getBytes() : new byte[0]);
        int paddingSize = calculatePadding(contentBytes.length);
        byte[] paddedContent = padContent(contentBytes, paddingSize);

        buffer.putLong(message.timestamp);
        buffer.put((byte) (message.isRelay ? 1 : 0));
        buffer.put((byte) (message.isPrivate ? 1 : 0));
        buffer.put((byte) (message.isEncrypted ? 1 : 0));
        buffer.putShort((short) (message.recipientNickname != null ? message.recipientNickname.length() : 0));
        if (message.recipientNickname != null) buffer.put(message.recipientNickname.getBytes());
        buffer.putShort((short) (message.senderPeerID != null ? message.senderPeerID.length() : 0));
        if (message.senderPeerID != null) buffer.put(message.senderPeerID.getBytes());
        buffer.putShort((short) paddedContent.length);
        buffer.put(paddedContent);

        return Arrays.copyOf(buffer.array(), buffer.position());
    }

    public static BitchatMessage decodeMessage(byte[] data) {
        if (data == null || data.length < 12) return null; // Minimum header size

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BitchatMessage message = new BitchatMessage();
        message.timestamp = buffer.getLong();
        message.isRelay = buffer.get() == 1;
        message.isPrivate = buffer.get() == 1;
        message.isEncrypted = buffer.get() == 1;
        short recipientLength = buffer.getShort();
        if (recipientLength > 0 && buffer.remaining() >= recipientLength) {
            byte[] recipientBytes = new byte[recipientLength];
            buffer.get(recipientBytes);
            message.recipientNickname = new String(recipientBytes);
        }
        short senderLength = buffer.getShort();
        if (senderLength > 0 && buffer.remaining() >= senderLength) {
            byte[] senderBytes = new byte[senderLength];
            buffer.get(senderBytes);
            message.senderPeerID = new String(senderBytes);
        }
        short contentLength = buffer.getShort();
        if (contentLength > 0 && buffer.remaining() >= contentLength) {
            byte[] contentBytes = new byte[contentLength];
            buffer.get(contentBytes);
            message.content = new String(unpadContent(contentBytes));
        } else {
            return null; // Invalid message due to insufficient data
        }

        return message;
    }

    private static byte[] padOrTruncate(byte[] data, int targetLength) {
        if (data.length == targetLength) return data;
        byte[] result = new byte[targetLength];
        Arrays.fill(result, (byte) 0);
        System.arraycopy(data, 0, result, 0, Math.min(data.length, targetLength));
        return result;
    }

    private static int calculatePadding(int length) {
        int blockSize = 256; // Adjustable block size
        int remainder = length % blockSize;
        return remainder == 0 ? 0 : blockSize - remainder;
    }

    private static byte[] padContent(byte[] content, int paddingSize) {
        byte[] padded = new byte[content.length + paddingSize];
        System.arraycopy(content, 0, padded, 0, content.length);
        Arrays.fill(padded, content.length, padded.length, (byte) paddingSize);
        return padded;
    }

    private static byte[] unpadContent(byte[] padded) {
        if (padded.length == 0) return padded;
        int paddingSize = padded[padded.length - 1] & 0xFF;
        if (paddingSize > padded.length) return padded;
        return Arrays.copyOf(padded, padded.length - paddingSize);
    }
}