import Foundation
import CommonCrypto

@objc(ChannelManager)
class ChannelManager: NSObject {
    private var joinedChannels = Set<String>()
    private var currentChannel: String?
    private var passwordProtectedChannels = Set<String>()
    private var channelKeys = [String: Data]()
    private var channelPasswords = [String: String]()
    private var channelCreators = [String: String]()
    private var channelKeyCommitments = [String: Data]()
    private var systemMessages = [BitchatMessage]()

    private let channelPattern = try! NSRegularExpression(pattern: "^#[a-zA-Z0-9-]+$", options: [])
    private let keychain = Keychain()
    private let encryption = Encryption()

    @objc func createChannel(_ channel: String, password: String?, creatorID: String) {
        guard isValidChannelName(channel) else {
            fatalError("Invalid channel name")
        }
        guard !joinedChannels.contains(channel) else { return }
        joinedChannels.insert(channel)
        currentChannel = channel
        channelCreators[channel] = creatorID
        if let password = password, !password.isEmpty {
            passwordProtectedChannels.insert(channel)
            channelPasswords[channel] = password
            let key = encryption.deriveChannelKey(password, channel: channel)
            channelKeys[channel] = key
            keychain.storeKey(key, identifier: "channel_\(channel)")
            let commitment = generateCommitment(key)
            channelKeyCommitments[channel] = commitment
            addSystemMessage("Channel \(channel) created with password protection.")
        } else {
            addSystemMessage("Channel \(channel) created.")
        }
    }

    @objc func joinChannel(_ channel: String, password: String?, peerID: String) -> Bool {
        guard isValidChannelName(channel) else {
            addSystemMessage("Invalid channel name: \(channel)")
            return false
        }
        guard joinedChannels.contains(channel) else {
            if passwordProtectedChannels.contains(channel) {
                guard let storedPassword = channelPasswords[channel], storedPassword == password else {
                    addSystemMessage("Incorrect password for \(channel)")
                    return false
                }
                let key = encryption.deriveChannelKey(password!, channel: channel)
                guard verifyCommitment(channel, key: key) else {
                    addSystemMessage("Key commitment verification failed for \(channel)")
                    return false
                }
                channelKeys[channel] = key
                keychain.storeKey(key, identifier: "channel_\(channel)")
            }
            joinedChannels.insert(channel)
            currentChannel = channel
            addSystemMessage("\(peerID) joined \(channel)")
            return true
        }
        currentChannel = channel
        return true
    }

    @objc func setChannelPassword(_ channel: String, password: String, peerID: String) {
        guard isValidChannelName(channel) else {
            fatalError("Invalid channel name")
        }
        guard joinedChannels.contains(channel), channelCreators[channel] == peerID else {
            fatalError("Only creator can set password")
        }
        passwordProtectedChannels.insert(channel)
        channelPasswords[channel] = password
        let key = encryption.deriveChannelKey(password, channel: channel)
        channelKeys[channel] = key
        keychain.storeKey(key, identifier: "channel_\(channel)")
        let commitment = generateCommitment(key)
        channelKeyCommitments[channel] = commitment
        addSystemMessage("Password set for \(channel)")
    }

    @objc func removeChannelPassword(_ channel: String, peerID: String) {
        guard isValidChannelName(channel) else {
            fatalError("Invalid channel name")
        }
        guard joinedChannels.contains(channel), channelCreators[channel] == peerID else {
            fatalError("Only creator can remove password")
        }
        passwordProtectedChannels.remove(channel)
        channelPasswords.removeValue(forKey: channel)
        channelKeys.removeValue(forKey: channel)
        channelKeyCommitments.removeValue(forKey: channel)
        addSystemMessage("Password removed from \(channel)")
    }

    @objc func receiveMessage(_ message: BitchatMessage) {
        if let channel = message.channel, joinedChannels.contains(channel) {
            if message.isEncrypted {
                if let key = channelKeys[channel], verifyCommitment(channel, key: key) {
                    // Decryption logic would go here
                    addSystemMessage("Received encrypted message in \(channel)")
                } else {
                    addSystemMessage("Unable to decrypt message in \(channel)")
                }
            } else {
                addSystemMessage("Received message in \(currentChannel ?? ""): \(message.content ?? "")")
            }
        }
    }

    @objc func processCommand(_ command: String, peerID: String) {
        let components = command.components(separatedBy: " ")
        guard let cmd = components.first?.lowercased() else { return }
        switch cmd {
        case "/join", "/j":
            if components.count > 1 {
                let channel = components[1]
                joinChannel(channel, password: nil, peerID: peerID)
            }
        default:
            addSystemMessage("Unknown command: \(command)")
        }
    }

    @objc func getSystemMessages() -> [BitchatMessage] {
        return systemMessages
    }

    @objc func transferOwnership(_ channel: String, newOwnerID: String, peerID: String) {
        guard isValidChannelName(channel) else {
            fatalError("Invalid channel name")
        }
        guard joinedChannels.contains(channel), channelCreators[channel] == peerID else {
            fatalError("Only creator can transfer ownership")
        }
        channelCreators[channel] = newOwnerID
        addSystemMessage("Ownership of \(channel) transferred to \(newOwnerID)")
    }

    private func addSystemMessage(_ content: String) {
        var msg = BitchatMessage()
        msg.sender = "system"
        msg.content = content
        msg.timestamp = Date().timeIntervalSince1970 * 1000
        msg.isEncrypted = false
        msg.senderPeerID = "system"
        msg.mentions = []
        msg.deliveryStatus = "DELIVERED"
        systemMessages.append(msg)
    }

    private func generateCommitment(_ key: Data) -> Data {
        var digest = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
        key.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(key.count), &digest)
        }
        return Data(digest)
    }

    private func verifyCommitment(_ channel: String, key: Data) -> Bool {
        guard let commitment = channelKeyCommitments[channel] else { return false }
        let computedCommitment = generateCommitment(key)
        return commitment == computedCommitment
    }

    private func isValidChannelName(_ channel: String) -> Bool {
        let range = NSRange(location: 0, length: channel.utf16.count)
        return channelPattern.firstMatch(in: channel, options: [], range: range) != nil
    }
}