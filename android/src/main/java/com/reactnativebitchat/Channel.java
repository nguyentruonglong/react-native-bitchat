package com.reactnativebitchat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Channel {
    private HashSet<String> joinedChannels = new HashSet<>();
    private String currentChannel;
    private HashSet<String> passwordProtectedChannels = new HashSet<>();
    private HashMap<String, byte[]> channelKeys = new HashMap<>();
    private HashMap<String, String> channelPasswords = new HashMap<>();
    private HashMap<String, String> channelCreators = new HashMap<>();
    private HashMap<String, byte[]> channelKeyCommitments = new HashMap<>();
    private ArrayList<BitchatMessage> systemMessages = new ArrayList<>();

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("^#[a-zA-Z0-9-]+$");
    private Keychain keychain = new Keychain();
    private Encryption encryption = new Encryption();

    public void createChannel(String channel, String password, String creatorID) {
        if (!CHANNEL_PATTERN.matcher(channel).matches()) {
            throw new IllegalArgumentException("Invalid channel name");
        }
        if (!joinedChannels.contains(channel)) {
            joinedChannels.add(channel);
            currentChannel = channel;
            channelCreators.put(channel, creatorID);
            if (password != null && !password.isEmpty()) {
                passwordProtectedChannels.add(channel);
                channelPasswords.put(channel, password);
                byte[] key = encryption.deriveChannelKey(password, channel);
                channelKeys.put(channel, key);
                keychain.storeKey(key, "channel_" + channel);
                byte[] commitment = generateCommitment(key);
                channelKeyCommitments.put(channel, commitment);
                addSystemMessage("Channel " + channel + " created with password protection.");
            } else {
                addSystemMessage("Channel " + channel + " created.");
            }
        }
    }

    public boolean joinChannel(String channel, String password, String peerID) {
        if (!CHANNEL_PATTERN.matcher(channel).matches()) {
            addSystemMessage("Invalid channel name: " + channel);
            return false;
        }
        if (joinedChannels.contains(channel)) {
            currentChannel = channel;
            return true;
        }
        if (passwordProtectedChannels.contains(channel)) {
            String storedPassword = channelPasswords.get(channel);
            if (storedPassword == null || !storedPassword.equals(password)) {
                addSystemMessage("Incorrect password for " + channel);
                return false;
            }
            byte[] key = encryption.deriveChannelKey(password, channel);
            if (!verifyCommitment(channel, key)) {
                addSystemMessage("Key commitment verification failed for " + channel);
                return false;
            }
            channelKeys.put(channel, key);
            keychain.storeKey(key, "channel_" + channel);
        }
        joinedChannels.add(channel);
        currentChannel = channel;
        addSystemMessage(peerID + " joined " + channel);
        return true;
    }

    public void setChannelPassword(String channel, String password, String peerID) {
        if (!CHANNEL_PATTERN.matcher(channel).matches()) {
            throw new IllegalArgumentException("Invalid channel name");
        }
        if (!joinedChannels.contains(channel) || !channelCreators.get(channel).equals(peerID)) {
            throw new SecurityException("Only creator can set password");
        }
        passwordProtectedChannels.add(channel);
        channelPasswords.put(channel, password);
        byte[] key = encryption.deriveChannelKey(password, channel);
        channelKeys.put(channel, key);
        keychain.storeKey(key, "channel_" + channel);
        byte[] commitment = generateCommitment(key);
        channelKeyCommitments.put(channel, commitment);
        addSystemMessage("Password set for " + channel);
    }

    public void removeChannelPassword(String channel, String peerID) {
        if (!CHANNEL_PATTERN.matcher(channel).matches()) {
            throw new IllegalArgumentException("Invalid channel name");
        }
        if (!joinedChannels.contains(channel) || !channelCreators.get(channel).equals(peerID)) {
            throw new SecurityException("Only creator can remove password");
        }
        passwordProtectedChannels.remove(channel);
        channelPasswords.remove(channel);
        channelKeys.remove(channel);
        channelKeyCommitments.remove(channel);
        addSystemMessage("Password removed from " + channel);
    }

    public void receiveMessage(BitchatMessage message) {
        if (message.channel != null && joinedChannels.contains(message.channel)) {
            if (message.isEncrypted) {
                String channel = message.channel;
                byte[] key = channelKeys.get(channel);
                if (key != null && verifyCommitment(channel, key)) {
                    // Decryption logic would go here
                    addSystemMessage("Received encrypted message in " + channel);
                } else {
                    addSystemMessage("Unable to decrypt message in " + channel);
                }
            } else {
                addSystemMessage("Received message in " + currentChannel + ": " + message.content);
            }
        }
    }

    public void processCommand(String command, String peerID) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 1) return;
        switch (parts[0].toLowerCase()) {
            case "/join":
            case "/j":
                if (parts.length > 1) {
                    String channel = parts[1];
                    joinChannel(channel, null, peerID); // Assume no password for command
                }
                break;
            default:
                addSystemMessage("Unknown command: " + command);
        }
    }

    public ArrayList<BitchatMessage> getSystemMessages() {
        return new ArrayList<>(systemMessages);
    }

    public void transferOwnership(String channel, String newOwnerID, String peerID) {
        if (!CHANNEL_PATTERN.matcher(channel).matches()) {
            throw new IllegalArgumentException("Invalid channel name");
        }
        if (!joinedChannels.contains(channel) || !channelCreators.get(channel).equals(peerID)) {
            throw new SecurityException("Only creator can transfer ownership");
        }
        channelCreators.put(channel, newOwnerID);
        addSystemMessage("Ownership of " + channel + " transferred to " + newOwnerID);
    }

    private void addSystemMessage(String content) {
        BitchatMessage msg = new BitchatMessage();
        msg.sender = "system";
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        msg.isEncrypted = false;
        msg.senderPeerID = "system";
        msg.mentions = new String[0];
        msg.deliveryStatus = "DELIVERED";
        systemMessages.add(msg);
    }

    private byte[] generateCommitment(byte[] key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(key);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private boolean verifyCommitment(String channel, byte[] key) {
        byte[] commitment = channelKeyCommitments.get(channel);
        if (commitment == null) return false;
        byte[] computedCommitment = generateCommitment(key);
        return java.util.Arrays.equals(commitment, computedCommitment);
    }
}