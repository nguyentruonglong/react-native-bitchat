package com.reactnativebitchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeliveryTracker {
    private final Map<String, String> messageStatuses = new HashMap<>();
    private final Map<String, List<DeliveryAck>> messageAcks = new HashMap<>();

    public void trackMessage(String messageID, String status) {
        if (status.equals("PENDING") || status.equals("DELIVERED") || status.equals("READ")) {
            messageStatuses.put(messageID, status);
        }
    }

    public DeliveryAck generateAck(String messageID, String recipientID, String nickname, int hopCount) {
        DeliveryAck ack = new DeliveryAck();
        ack.messageID = messageID;
        ack.recipientID = recipientID;
        ack.nickname = nickname;
        ack.hopCount = hopCount;
        ack.timestamp = System.currentTimeMillis();
        ack.id = UUID.randomUUID().toString();
        return ack;
    }

    public void processAck(DeliveryAck ack) {
        if (messageStatuses.containsKey(ack.messageID)) {
            if (ack.hopCount == 0) {
                trackMessage(ack.messageID, "DELIVERED");
            }
            messageAcks.computeIfAbsent(ack.messageID, k -> new ArrayList<>()).add(ack);
        }
    }

    public String getStatus(String messageID) {
        return messageStatuses.getOrDefault(messageID, "PENDING");
    }

    public List<DeliveryAck> getAcks(String messageID) {
        return new ArrayList<>(messageAcks.getOrDefault(messageID, new ArrayList<>()));
    }

    public static class DeliveryAck {
        public String id;
        public String messageID;
        public String recipientID;
        public String nickname;
        public int hopCount;
        public long timestamp;
    }
}