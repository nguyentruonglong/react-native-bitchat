import Foundation

class DeliveryTracker {
    private var messageStatuses = [String: String]()
    private var messageAcks = [String: [DeliveryAck]]()

    func trackMessage(_ messageID: String, status: String) {
        if ["PENDING", "DELIVERED", "READ"].contains(status) {
            messageStatuses[messageID] = status
        }
    }

    func generateAck(messageID: String, recipientID: String, nickname: String, hopCount: Int) -> DeliveryAck {
        var ack = DeliveryAck()
        ack.id = UUID().uuidString
        ack.messageID = messageID
        ack.recipientID = recipientID
        ack.nickname = nickname
        ack.hopCount = hopCount
        ack.timestamp = Date().timeIntervalSince1970 * 1000
        return ack
    }

    func processAck(_ ack: DeliveryAck) {
        if messageStatuses[ack.messageID] != nil {
            if ack.hopCount == 0 {
                trackMessage(ack.messageID, status: "DELIVERED")
            }
            var acks = messageAcks[ack.messageID, default: []]
            acks.append(ack)
            messageAcks[ack.messageID] = acks
        }
    }

    func getStatus(_ messageID: String) -> String? {
        return messageStatuses[messageID] ?? "PENDING"
    }

    func getAcks(_ messageID: String) -> [DeliveryAck] {
        return messageAcks[messageID] ?? []
    }

    struct DeliveryAck {
        var id: String
        var messageID: String
        var recipientID: String
        var nickname: String
        var hopCount: Int
        var timestamp: Double
    }
}