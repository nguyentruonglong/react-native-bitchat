package com.reactnativebitchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BleService {
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser advertiser;
    private BluetoothLeScanner scanner;
    private final Map<String, byte[]> fragmentBuffers = new HashMap<>();
    private final Protocol protocol = new Protocol();

    public BleService() {
        Context context = null; // Replace with actual context from ReactApplicationContext
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    public void startAdvertising(String peerID) {
        if (peerID.length() != 8) throw new IllegalArgumentException("Peer ID must be 8 bytes");
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();
        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(UUID.randomUUID())) // Unique service UUID
                .addServiceData(new ParcelUuid(UUID.randomUUID()), peerID.getBytes())
                .build();
        advertiser.startAdvertising(settings, data, new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                sendCoverTraffic(peerID);
            }
            @Override
            public void onStartFailure(int errorCode) {
                throw new RuntimeException("Advertising failed: " + errorCode);
            }
        });
    }

    public void sendCoverTraffic(String peerID) {
        byte[] coverData = new byte[20]; // Example cover traffic
        new java.util.Random().nextBytes(coverData);
        AdvertiseData cover = new AdvertiseData.Builder()
                .addServiceData(new ParcelUuid(UUID.randomUUID()), coverData)
                .build();
        advertiser.startAdvertising(new AdvertiseSettings.Builder().build(), cover, new AdvertiseCallback() {});
    }

    public String[] scanPeers() {
        List<String> peers = new ArrayList<>();
        scanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                byte[] scanData = result.getScanRecord().getServiceData().values().stream().findFirst().orElse(null);
                if (scanData != null && scanData.length >= 8) {
                    String peerID = new String(Arrays.copyOfRange(scanData, 0, 8));
                    peers.add(peerID);
                }
            }
        });
        try {
            Thread.sleep(5000); // Scan for 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        scanner.stopScan(null);
        return peers.toArray(new String[0]);
    }

    public void sendMessage(BitchatMessage message, String recipient) {
        BitchatPacket packet = new BitchatPacket();
        packet.version = 1;
        packet.type = 0; // Message type
        packet.senderID = peerIDToBytes("senderID"); // Replace with actual peerID
        packet.recipientID = recipient != null ? peerIDToBytes(recipient) : new byte[8]; // Broadcast if null
        packet.timestamp = System.currentTimeMillis();
        packet.payload = protocol.encodeMessage(message);
        packet.ttl = 5;
        sendPacket(packet);
    }

    public void sendEncryptedChannelMessage(BitchatMessage message, String channel) {
        BitchatPacket packet = new BitchatPacket();
        packet.version = 1;
        packet.type = 1; // Encrypted channel message type
        packet.senderID = peerIDToBytes("senderID"); // Replace with actual peerID
        packet.recipientID = new byte[8]; // Broadcast to channel
        packet.timestamp = System.currentTimeMillis();
        packet.payload = protocol.encodeMessage(message);
        packet.ttl = 5;
        sendPacket(packet);
    }

    private void sendPacket(BitchatPacket packet) {
        byte[] data = protocol.encodePacket(packet);
        int fragmentSize = 20; // Example fragment size
        for (int i = 0; i < data.length; i += fragmentSize) {
            int end = Math.min(i + fragmentSize, data.length);
            byte[] fragment = Arrays.copyOfRange(data, i, end);
            packet.type = i == 0 ? Protocol.FRAGMENT_START : (end == data.length ? Protocol.FRAGMENT_END : Protocol.FRAGMENT_CONTINUE);
            AdvertiseData fragmentData = new AdvertiseData.Builder()
                    .addServiceData(new ParcelUuid(UUID.randomUUID()), fragment)
                    .build();
            advertiser.startAdvertising(new AdvertiseSettings.Builder().build(), fragmentData, new AdvertiseCallback() {});
        }
    }

    private byte[] peerIDToBytes(String peerID) {
        byte[] bytes = new byte[8];
        byte[] idBytes = peerID.getBytes();
        System.arraycopy(idBytes, 0, bytes, 0, Math.min(idBytes.length, 8));
        return bytes;
    }
}