import XCTest
@testable import react_native_bitchat

class ChannelTests: XCTestCase {
    func testKeyDerivation() {
        let channel = ChannelManager()
        channel.createChannel("#test", password: "password", creatorID: "creator")
        XCTAssertNotNil(channel.channelKeys["#test"])
        XCTAssertEqual(32, channel.channelKeys["#test"]?.count)
    }

    // Add similar tests for join unprotected/protected, encryption/decryption, password management, ownership, commands as in Java
}