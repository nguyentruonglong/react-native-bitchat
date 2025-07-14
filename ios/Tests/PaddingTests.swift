import XCTest
@testable import react_native_bitchat

class PaddingTests: XCTestCase {
    func testBasicPadding() {
        let data = "test".data(using: .utf8)!
        let targetSize = Message.optimalBlockSize(for: data.count)
        let padded = Message.pad(data, toSize: targetSize)
        let unpadded = Message.unpad(padded)
        XCTAssertEqual(data, unpadded)
    }

    // Add similar tests for multiple block sizes, large data, invalid padding, randomness as in Java
}