package com.reactnativebitchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Message {
    public static class BitchatPacket {
        public int version;
        public int type;
        public byte[] senderID = new byte[8];
        public byte[] recipientID = new byte[8]; // 8-byte or broadcast (all zeros)
        public long timestamp;
        public byte[] payload;
        public byte[] signature; // Optional 64-byte
        public int ttl;
    }

    public static class BitchatMessage {
        public String id; // UUID
        public String sender;
        public String content;
        public double timestamp;
        public boolean isRelay;
        public String originalSender;
        public boolean isPrivate;
        public String recipientNickname;
        public String senderPeerID;
        public ArrayList<String> mentions = new ArrayList<>();
        public String channel;
        public byte[] encryptedContent;
        public boolean isEncrypted;
        public String deliveryStatus;
    }

    public static byte[] pad(byte[] data, int targetSize) {
        if (data == null) return null;
        int paddingSize = targetSize - (data.length % targetSize);
        if (paddingSize > 255) paddingSize = 255; // Limit to 255 bytes
        byte[] padding = new byte[paddingSize];
        new Random().nextBytes(padding); // Random PKCS#7 padding
        padding[padding.length - 1] = (byte) paddingSize; // Store padding size
        byte[] padded = new byte[data.length + padding.length];
        System.arraycopy(data, 0, padded, 0, data.length);
        System.arraycopy(padding, 0, padded, data.length, padding.length);
        return padded;
    }

    public static byte[] unpad(byte[] data) {
        if (data == null || data.length == 0) return data;
        int paddingSize = data[data.length - 1] & 0xFF;
        if (paddingSize > 255 || paddingSize > data.length) return data; // Invalid padding
        return Arrays.copyOf(data, data.length - paddingSize);
    }

    public static int optimalBlockSize(int dataSize) {
        int[] blockSizes = {256, 512, 1024, 2048};
        for (int size : blockSizes) {
            int padding = size - (dataSize % size);
            if (padding <= 255) return size;
        }
        return dataSize; // Return original if no suitable block size
    }
}