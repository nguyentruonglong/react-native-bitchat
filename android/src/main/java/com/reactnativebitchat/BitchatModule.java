package com.reactnativebitchat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;

import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.Set;

public class BitchatModule extends ReactContextBaseJavaModule {
    private final Channel channel = new Channel();
    private final BleService bleService = new BleService();
    private final Encryption encryption = new Encryption();
    private Callback messageCallback, peerConnectedCallback, peerDisconnectedCallback, deliveryAckCallback,
            readReceiptCallback, deliveryStatusUpdateCallback;
    private static final String PREFS_NAME = "BitchatPrefs";
    private static final String FAVORITES_KEY = "Favorites";

    public BitchatModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "Bitchat";
    }

    @ReactMethod
    public void startAdvertising(String peerID, Promise promise) {
        if (peerID.length() != 8) {
            promise.reject("INVALID_PEER_ID", "Peer ID must be 8 bytes");
            return;
        }
        try {
            bleService.startAdvertising(peerID);
            bleService.sendCoverTraffic(peerID); // Include cover traffic
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("ADVERTISE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void scanPeers(Promise promise) {
        try {
            String[] peers = bleService.scanPeers();
            WritableArray result = new WritableNativeArray();
            for (String peer : peers) result.pushString(peer);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject("SCAN_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void sendMessage(ReadableMap message, String recipient, Promise promise) {
        try {
            BitchatMessage msg = mapToBitchatMessage(message);
            msg.deliveryStatus = "PENDING";
            if (recipient != null) msg.recipientNickname = recipient;
            bleService.sendMessage(msg, recipient);
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("SEND_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void sendEncryptedChannelMessage(ReadableMap message, String channel, Promise promise) {
        try {
            BitchatMessage msg = mapToBitchatMessage(message);
            byte[] key = encryption.deriveChannelKey(channel, channel); // Use channel as password for simplicity
            msg.encryptedContent = encryption.encryptContent(msg.content, key);
            msg.isEncrypted = true;
            msg.channel = channel;
            bleService.sendEncryptedChannelMessage(msg, channel);
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("ENCRYPT_SEND_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void onMessageReceived(Callback callback) {
        messageCallback = callback;
    }

    @ReactMethod
    public void onPeerConnected(Callback callback) {
        peerConnectedCallback = callback;
    }

    @ReactMethod
    public void onPeerDisconnected(Callback callback) {
        peerDisconnectedCallback = callback;
    }

    @ReactMethod
    public void onDeliveryAck(Callback callback) {
        deliveryAckCallback = callback;
    }

    @ReactMethod
    public void onReadReceipt(Callback callback) {
        readReceiptCallback = callback;
    }

    @ReactMethod
    public void onDeliveryStatusUpdate(Callback callback) {
        deliveryStatusUpdateCallback = callback;
    }

    @ReactMethod
    public void decryptChannelMessage(String encryptedContent, String channel, Promise promise) {
        try {
            byte[] key = encryption.deriveChannelKey(channel, channel); // Use channel as password
            String decrypted = encryption.decryptContent(encryptedContent.getBytes(), key);
            promise.resolve(decrypted);
        } catch (Exception e) {
            promise.reject("DECRYPT_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void isFavorite(String fingerprint, Promise promise) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            promise.reject("INVALID_FINGERPRINT", "Fingerprint cannot be empty");
            return;
        }
        SharedPreferences prefs = getReactApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, null);
        boolean isFavorite = favorites != null && favorites.contains(fingerprint);
        promise.resolve(isFavorite);
    }

    @ReactMethod
    public void joinChannel(String channel, String password, Promise promise) {
        try {
            boolean success = channel.joinChannel(channel, password, "peerID");
            promise.resolve(success);
        } catch (Exception e) {
            promise.reject("JOIN_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void setChannelPassword(String channel, String password, Promise promise) {
        try {
            channel.setChannelPassword(channel, password, "peerID");
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("SET_PASSWORD_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void removeChannelPassword(String channel, Promise promise) {
        try {
            channel.removeChannelPassword(channel, "peerID");
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("REMOVE_PASSWORD_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void transferChannelOwnership(String channel, String newOwnerID, Promise promise) {
        try {
            channel.transferOwnership(channel, newOwnerID, "peerID");
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject("TRANSFER_ERROR", e.getMessage());
        }
    }

    private BitchatMessage mapToBitchatMessage(ReadableMap map) {
        BitchatMessage msg = new BitchatMessage();
        msg.id = map.getString("id");
        msg.sender = map.getString("sender");
        msg.content = map.getString("content");
        msg.timestamp = map.getDouble("timestamp");
        msg.isRelay = map.getBoolean("isRelay");
        if (map.hasKey("originalSender")) msg.originalSender = map.getString("originalSender");
        msg.isPrivate = map.getBoolean("isPrivate");
        if (map.hasKey("recipientNickname")) msg.recipientNickname = map.getString("recipientNickname");
        msg.senderPeerID = map.getString("senderPeerID");
        // Handle mentions, channel, encryptedContent, isEncrypted, deliveryStatus as needed
        return msg;
    }
}