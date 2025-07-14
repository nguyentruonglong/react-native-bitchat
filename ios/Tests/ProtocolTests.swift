import XCTest
@testable import react_native_bitchat

class ProtocolTests: XCTestCase {
    func testPacketEncodingDecoding() {
        var packet = BitchatPacket()
        packet.version = 1
        packet.type = 0
        packet.senderID = Data([1, 2, 3, 4, 5, 6, 7, 8])
        packet.recipientID = Data(count: 8) // Broadcast
        packet.timestamp = Date().timeIntervalSince1970 * 1000
        packet.payload = Data([9, 10])
        packet.ttl = 5

        let encoded = Protocol.encodePacket(packet)
        guard let decoded = Protocol.decodePacket(encoded) else {
            XCTFail("Decoding failed")
            return
        }
        XCTAssertEqual(packet.version, decoded.version)
        XCTAssertEqual(packet.senderID, decoded.senderID)
        XCTAssertEqual(packet.recipientID, decoded.recipientID)
        XCTAssertEqual(packet.timestamp, decoded.timestamp, accuracy: 1)
        XCTAssertEqual(packet.payload, decoded.payload)
        XCTAssertEqual(packet.ttl - 1, decoded.ttl)
    }

    // Add similar tests for broadcast, signature, invalid inputs as in Java
}