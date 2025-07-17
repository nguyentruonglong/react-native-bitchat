import Foundation
import CommonCrypto

class Encryption {
    static func deriveChannelKey(_ password: String, channel: String) -> Data {
        let passwordData = password.data(using: .utf8)!
        let salt = channel.data(using: .utf8)!
        let keyLength = 32 // 32 bytes = 256 bits
        let iterations = 100000
        var derivedKey = [UInt8](repeating: 0, count: keyLength)

        let result = CCKeyDerivationPBKDF(
            CCPBKDFAlgorithm(kCCPBKDF2),
            passwordData.withUnsafeBytes { $0.baseAddress?.assumingMemoryBound(to: Int8.self) },
            passwordData.count,
            salt.withUnsafeBytes { $0.baseAddress?.assumingMemoryBound(to: Int8.self) },
            salt.count,
            CCPseudoRandomAlgorithm(kCCPRFHmacAlgSHA256),
            UInt32(iterations),
            &derivedKey,
            keyLength
        )

        guard result == kCCSuccess else {
            fatalError("Key derivation failed")
        }

        return Data(derivedKey)
    }
}