import Foundation

@objc(Bitchat)
class BitchatModule: NSObject {
    private let channel = ChannelManager()
    private let bleService = BleService()
    private let encryption = Encryption()
    private var messageCallback: RCTResponseSenderBlock?
    private var peerConnectedCallback: RCTResponseSenderBlock?
    private var peerDisconnectedCallback: RCTResponseSenderBlock?
    private var deliveryAckCallback: RCTResponseSenderBlock?
    private var readReceiptCallback: RCTResponseSenderBlock?
    private var deliveryStatusUpdateCallback: RCTResponseSenderBlock?
    private let favoritesKey = "BitchatFavorites"

    @objc func startAdvertising(peerID: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        guard peerID.count == 8 else {
            rejecter("INVALID_PEER_ID", nil, NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Peer ID must be 8 bytes"]))
            return
        }
        do {
            try bleService.startAdvertising(peerID: peerID)
            try bleService.sendCoverTraffic(peerID: peerID) // Include cover traffic
            resolver(nil)
        } catch {
            rejecter("ADVERTISE_ERROR", nil, error)
        }
    }

    @objc func scanPeers(resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            let peers = try bleService.scanPeers()
            resolver(peers.map { $0 as Any })
        } catch {
            rejecter("SCAN_ERROR", nil, error)
        }
    }

    @objc func sendMessage(message: [String: Any], recipient: String?, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        guard let msg = mapToBitchatMessage(message) else {
            rejecter("INVALID_MESSAGE", nil, NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid message format"]))
            return
        }
        do {
            msg.deliveryStatus = "PENDING"
            if let recipient = recipient { msg.recipientNickname = recipient }
            try bleService.sendMessage(msg, recipient: recipient)
            resolver(nil)
        } catch {
            rejecter("SEND_ERROR", nil, error)
        }
    }

    @objc func sendEncryptedChannelMessage(message: [String: Any], channel: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        guard let msg = mapToBitchatMessage(message) else {
            rejecter("INVALID_MESSAGE", nil, NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid message format"]))
            return
        }
        do {
            let key = encryption.deriveChannelKey(channel, channel: channel) // Use channel as password
            if let content = msg.content {
                msg.encryptedContent = encryption.encryptContent(content, key: key)
                msg.isEncrypted = true
                msg.channel = channel
                try bleService.sendEncryptedChannelMessage(msg, channel: channel)
                resolver(nil)
            } else {
                rejecter("NO_CONTENT", nil, NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Message content required"]))
            }
        } catch {
            rejecter("ENCRYPT_SEND_ERROR", nil, error)
        }
    }

    @objc func onMessageReceived(callback: @escaping RCTResponseSenderBlock) {
        messageCallback = callback
    }

    @objc func onPeerConnected(callback: @escaping RCTResponseSenderBlock) {
        peerConnectedCallback = callback
    }

    @objc func onPeerDisconnected(callback: @escaping RCTResponseSenderBlock) {
        peerDisconnectedCallback = callback
    }

    @objc func onDeliveryAck(callback: @escaping RCTResponseSenderBlock) {
        deliveryAckCallback = callback
    }

    @objc func onReadReceipt(callback: @escaping RCTResponseSenderBlock) {
        readReceiptCallback = callback
    }

    @objc func onDeliveryStatusUpdate(callback: @escaping RCTResponseSenderBlock) {
        deliveryStatusUpdateCallback = callback
    }

    @objc func decryptChannelMessage(encryptedContent: String, channel: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            let key = encryption.deriveChannelKey(channel, channel: channel)
            let decrypted = try encryption.decryptContent(Data(encryptedContent.utf8), key: key)
            resolver(String(data: decrypted, encoding: .utf8))
        } catch {
            rejecter("DECRYPT_ERROR", nil, error)
        }
    }

    @objc func isFavorite(fingerprint: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        guard !fingerprint.isEmpty else {
            rejecter("INVALID_FINGERPRINT", nil, NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Fingerprint cannot be empty"]))
            return
        }
        let favorites = UserDefaults.standard.array(forKey: favoritesKey) as? [String] ?? []
        let isFavorite = favorites.contains(fingerprint)
        resolver(isFavorite)
    }

    @objc func joinChannel(channel: String, password: String?, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            let success = channel.joinChannel(channel, password: password, peerID: "peerID")
            resolver(success)
        } catch {
            rejecter("JOIN_ERROR", nil, error)
        }
    }

    @objc func setChannelPassword(channel: String, password: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            channel.setChannelPassword(channel, password: password, peerID: "peerID")
            resolver(nil)
        } catch {
            rejecter("SET_PASSWORD_ERROR", nil, error)
        }
    }

    @objc func removeChannelPassword(channel: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            channel.removeChannelPassword(channel, peerID: "peerID")
            resolver(nil)
        } catch {
            rejecter("REMOVE_PASSWORD_ERROR", nil, error)
        }
    }

    @objc func transferChannelOwnership(channel: String, newOwnerID: String, resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        do {
            channel.transferOwnership(channel, newOwnerID: newOwnerID, peerID: "peerID")
            resolver(nil)
        } catch {
            rejecter("TRANSFER_ERROR", nil, error)
        }
    }

    private func mapToBitchatMessage(_ map: [String: Any]) -> BitchatMessage? {
        var msg = BitchatMessage()
        msg.id = map["id"] as? String
        msg.sender = map["sender"] as? String
        msg.content = map["content"] as? String
        msg.timestamp = (map["timestamp"] as? NSNumber)?.doubleValue ?? Date().timeIntervalSince1970 * 1000
        msg.isRelay = map["isRelay"] as? Bool ?? false
        msg.originalSender = map["originalSender"] as? String
        msg.isPrivate = map["isPrivate"] as? Bool ?? false
        msg.recipientNickname = map["recipientNickname"] as? String
        msg.senderPeerID = map["senderPeerID"] as? String
        msg.mentions = map["mentions"] as? [String] ?? []
        msg.channel = map["channel"] as? String
        msg.encryptedContent = map["encryptedContent"] as? Data
        msg.isEncrypted = map["isEncrypted"] as? Bool ?? false
        msg.deliveryStatus = map["deliveryStatus"] as? String ?? "PENDING"
        return msg
    }

    // Helper method to add a favorite (not exposed to JS, for internal use if needed)
    private func addFavorite(_ fingerprint: String) {
        var favorites = UserDefaults.standard.array(forKey: favoritesKey) as? [String] ?? []
        if !favorites.contains(fingerprint) {
            favorites.append(fingerprint)
            UserDefaults.standard.set(favorites, forKey: favoritesKey)
        }
    }

    // Helper method to remove a favorite (not exposed to JS, for internal use if needed)
    private func removeFavorite(_ fingerprint: String) {
        var favorites = UserDefaults.standard.array(forKey: favoritesKey) as? [String] ?? []
        if let index = favorites.firstIndex(of: fingerprint) {
            favorites.remove(at: index)
            UserDefaults.standard.set(favorites, forKey: favoritesKey)
        }
    }
}