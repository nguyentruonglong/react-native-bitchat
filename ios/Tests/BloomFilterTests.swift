import XCTest
@testable import react_native_bitchat

class BloomFilterTests: XCTestCase {
    func testInsertionAndLookup() {
        let filter = BloomFilter(size: 1000, falsePositiveRate: 0.01)
        filter.insert("test")
        XCTAssertTrue(filter.mightContain("test"))
        XCTAssertFalse(filter.mightContain("notest"))
    }

    // Add similar tests for false positive, reset, hash distribution, adaptive sizing as in Java
}