import Foundation

class Protocol {
    static let VERSION: UInt8 = 1
    static let HEADER_SIZE = 29 // version(1) + type(1) + senderID(8) + recipientID(8) + timestamp(8) + ttl(1) + payloadLength(2)
    static let FRAGMENT_START: UInt8 = 0x01
    static let FRAGMENT_CONTINUE: UInt8 = 0x02
    static let FRAGMENT_END: UInt8 = 0x03
    private static var fragmentBuffer: [String: Data] = [:]

    static func encodePacket(_ packet: BitchatPacket) -> Data {
        var ttl = packet.ttl
        if ttl <= 0 { ttl = 1 } // Ensure TTL is at least 1
        ttl -= 1 // Decrement TTL for routing

        var data = Data(capacity: HEADER_SIZE + (packet.payload?.count ?? 0) + (packet.signature?.count ?? 0))
        if let payload = packet.payload, payload.count > 1024 { // Arbitrary max fragment size
            var fragmentType: UInt8 = FRAGMENT_START
            var offset = 0
            while offset < payload.count {
                let chunkSize = min(1024, payload.count - offset)
                let chunk = payload[offset..<offset + chunkSize]
                var fragmentPacket = packet
                fragmentPacket.type = Int(fragmentType)
                fragmentPacket.payload = Data(chunk)
                data.append(contentsOf: [VERSION, UInt8(fragmentPacket.type)])
                data.append(padOrTruncate(fragmentPacket.senderID, to: 8))
                data.append(padOrTruncate(fragmentPacket.recipientID, to: 8))
                data.append(contentsOf: withUnsafeBytes(of: fragmentPacket.timestamp.bigEndian) { Array($0) })
                data.append(UInt8(ttl))
                let chunkLength = UInt16(chunkSize)
                data.append(contentsOf: withUnsafeBytes(of: chunkLength.bigEndian) { Array($0) })
                data.append(chunk)
                if let signature = fragmentPacket.signature { data.append(signature) }
                offset += chunkSize
                fragmentType = FRAGMENT_CONTINUE
            }
            data.append(FRAGMENT_END)
        } else {
            data.append(contentsOf: [VERSION, UInt8(packet.type)])
            data.append(padOrTruncate(packet.senderID, to: 8))
            data.append(padOrTruncate(packet.recipientID, to: 8))
            data.append(contentsOf: withUnsafeBytes(of: packet.timestamp.bigEndian) { Array($0) })
            data.append(UInt8(ttl))
            let payloadLength = UInt16(packet.payload?.count ?? 0)
            data.append(contentsOf: withUnsafeBytes(of: payloadLength.bigEndian) { Array($0) })
            if let payload = packet.payload { data.append(payload) }
            if let signature = packet.signature { data.append(signature) }
        }

        return data
    }

    static func decodePacket(_ data: Data) -> BitchatPacket? {
        guard data.count >= HEADER_SIZE else { return nil }

        let version = data[0]
        guard version == VERSION else { return nil } // Validate version

        var packet = BitchatPacket()
        packet.version = Int(version)
        packet.type = Int(data[1])
        packet.senderID = data[2..<10].copyBytes()
        packet.recipientID = data[10..<18].copyBytes()
        packet.timestamp = data.withUnsafeBytes { $0.load(fromByteOffset: 18, as: Int64.self).bigEndian }
        packet.ttl = Int(data[26])
        let payloadLength = data.withUnsafeBytes { $0.load(fromByteOffset: 27, as: UInt16.self).bigEndian }
        let payloadOffset = 29

        // Handle fragmentation
        let key = String(decoding: packet.senderID, as: UTF8.self) + "_" + String(packet.timestamp)
        if packet.type == Int(FRAGMENT_START) || packet.type == Int(FRAGMENT_CONTINUE) || packet.type == Int(FRAGMENT_END) {
            guard payloadLength > 0, data.count >= payloadOffset + Int(payloadLength) else { return nil }
            let fragmentData = data[payloadOffset..<payloadOffset + Int(payloadLength)]
            if var existing = fragmentBuffer[key] {
                existing.append(fragmentData)
                fragmentBuffer[key] = existing
            } else {
                fragmentBuffer[key] = fragmentData
            }

            if packet.type == Int(FRAGMENT_END) {
                packet.payload = fragmentBuffer.removeValue(forKey: key)
                let signatureOffset = payloadOffset + Int(payloadLength)
                if data.count >= signatureOffset + 64 {
                    packet.signature = data[signatureOffset..<signatureOffset + 64]
                }
                return packet
            }
            return nil // Wait for more fragments
        }

        if payloadLength > 0, data.count >= payloadOffset + Int(payloadLength) {
            packet.payload = data[payloadOffset..<payloadOffset + Int(payloadLength)]
        }
        let signatureOffset = payloadOffset + Int(payloadLength)
        if data.count >= signatureOffset + 64 {
            packet.signature = data[signatureOffset..<signatureOffset + 64]
        }

        return packet
    }

    static func encodeMessage(_ message: BitchatMessage) -> Data {
        var data = Data()
        let contentData = message.content?.data(using: .utf8) ?? Data()
        let paddingSize = calculatePadding(contentData.count)
        let paddedContent = padContent(contentData, paddingSize: paddingSize)

        var timestampBytes = withUnsafeBytes(of: UInt64(bitPattern: UInt64(message.timestamp))) { Array($0) }
        timestampBytes.reverse() // Convert to big-endian
        data.append(contentsOf: timestampBytes)
        data.append(message.isRelay ? [1 as UInt8] : [0 as UInt8])
        data.append(message.isPrivate ? [1 as UInt8] : [0 as UInt8])
        data.append(message.isEncrypted ? [1 as UInt8] : [0 as UInt8])
        let recipientLength = UInt16(message.recipientNickname?.count ?? 0)
        var recipientLengthBytes = withUnsafeBytes(of: recipientLength.bigEndian) { Array($0) }
        data.append(contentsOf: recipientLengthBytes)
        if let recipient = message.recipientNickname?.data(using: .utf8) {
            data.append(recipient)
        }
        let senderLength = UInt16(message.senderPeerID?.count ?? 0)
        var senderLengthBytes = withUnsafeBytes(of: senderLength.bigEndian) { Array($0) }
        data.append(contentsOf: senderLengthBytes)
        if let sender = message.senderPeerID?.data(using: .utf8) {
            data.append(sender)
        }
        let contentLength = UInt16(paddedContent.count)
        var contentLengthBytes = withUnsafeBytes(of: contentLength.bigEndian) { Array($0) }
        data.append(contentsOf: contentLengthBytes)
        data.append(paddedContent)

        return data
    }

    static func decodeMessage(_ data: Data) -> BitchatMessage? {
        guard data.count >= 12 else { return nil } // Minimum header size

        var message = BitchatMessage()
        message.timestamp = data.withUnsafeBytes { $0.load(fromByteOffset: 0, as: Int64.self).bigEndian }
        message.isRelay = data[8] == 1
        message.isPrivate = data[9] == 1
        message.isEncrypted = data[10] == 1
        let recipientLength = data.withUnsafeBytes { $0.load(fromByteOffset: 11, as: UInt16.self).bigEndian }
        var offset = 13
        if recipientLength > 0, data.count >= offset + Int(recipientLength) {
            let recipientData = data[offset..<offset + Int(recipientLength)]
            message.recipientNickname = String(data: recipientData, encoding: .utf8)
            offset += Int(recipientLength)
        }
        let senderLength = data.withUnsafeBytes { $0.load(fromByteOffset: offset, as: UInt16.self).bigEndian }
        offset += 2
        if senderLength > 0, data.count >= offset + Int(senderLength) {
            let senderData = data[offset..<offset + Int(senderLength)]
            message.senderPeerID = String(data: senderData, encoding: .utf8)
            offset += Int(senderLength)
        }
        let contentLength = data.withUnsafeBytes { $0.load(fromByteOffset: offset, as: UInt16.self).bigEndian }
        offset += 2
        if contentLength > 0, data.count >= offset + Int(contentLength) {
            let contentData = data[offset..<offset + Int(contentLength)]
            message.content = String(data: unpadContent(contentData), encoding: .utf8)
        } else {
            return nil
        }

        return message
    }

    private static func padOrTruncate(_ data: Data, to length: Int) -> Data {
        var result = Data(count: length)
        result.replaceSubrange(0..<min(data.count, length), with: data)
        return result
    }

    private static func calculatePadding(_ length: Int) -> Int {
        let blockSize = 256
        let remainder = length % blockSize
        return remainder == 0 ? 0 : blockSize - remainder
    }

    private static func padContent(_ content: Data, paddingSize: Int) -> Data {
        var padded = content
        let padding = Data(count: paddingSize)
        padded.append(padding)
        return padded
    }

    private static func unpadContent(_ padded: Data) -> Data {
        guard let paddingSize = padded.last else { return padded }
        let padLength = Int(paddingSize)
        guard padLength <= padded.count else { return padded }
        return padded.subdata(in: 0..<(padded.count - padLength))
    }
}