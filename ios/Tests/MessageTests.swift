import XCTest
@testable import react_native_bitchat

class MessageTests: XCTestCase {
    func testMessageEncodingDecoding() {
        var msg = BitchatMessage()
        msg.id = "123e4567-e89b-12d3-a456-426614174000"
        msg.sender = "user"
        msg.content = "Hello"
        msg.timestamp = Date().timeIntervalSince1970 * 1000
        let encoded = Protocol.encodeMessage(msg)
        guard let decoded = Protocol.decodeMessage(encoded) else {
            XCTFail("Decoding failed")
            return
        }
        XCTAssertEqual(msg.content, decoded.content)
    }

    // Add similar tests for room, encrypted, private, relay, empty/long as in Java
}