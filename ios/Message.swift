import Foundation

struct BitchatPacket {
    var version: Int
    var type: Int
    var senderID: Data = Data(count: 8) // 8-byte fixed
    var recipientID: Data = Data(count: 8) // 8-byte or broadcast (all zeros)
    var timestamp: Int64
    var payload: Data?
    var signature: Data? // Optional 64-byte
    var ttl: Int
}

struct BitchatMessage {
    var id: String // UUID
    var sender: String
    var content: String?
    var timestamp: Double
    var isRelay: Bool
    var originalSender: String?
    var isPrivate: Bool
    var recipientNickname: String?
    var senderPeerID: String
    var mentions: [String] = []
    var channel: String?
    var encryptedContent: Data?
    var isEncrypted: Bool
    var deliveryStatus: String
}

extension Message {
    static func pad(_ data: Data, toSize targetSize: Int) -> Data {
        guard !data.isEmpty else { return data }
        let remainder = data.count % targetSize
        let paddingSize = remainder == 0 ? 0 : targetSize - remainder
        let limitedPadding = min(paddingSize, 255)
        var padding = Data(count: limitedPadding)
        let random = SystemRandomNumberGenerator()
        padding = Data((0..<limitedPadding).map { _ in UInt8.random(in: 0...255, using: &random) })
        padding[padding.count - 1] = UInt8(limitedPadding) // PKCS#7 padding with size
        var padded = data
        padded.append(padding)
        return padded
    }

    static func unpad(_ data: Data) -> Data {
        guard !data.isEmpty else { return data }
        guard let paddingSize = data.last, paddingSize <= 255, Int(paddingSize) <= data.count else { return data }
        return data.subdata(in: 0..<(data.count - Int(paddingSize)))
    }

    static func optimalBlockSize(for dataSize: Int) -> Int {
        let blockSizes = [256, 512, 1024, 2048]
        for size in blockSizes {
            let padding = size - (dataSize % size)
            if padding <= 255 { return size }
        }
        return dataSize // Return original if no suitable block size
    }
}