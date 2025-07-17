export interface BitchatPacket {
  version: number;
  type: number;
  senderID: string;
  recipientID: string;
  timestamp: number;
  payload: string;
  signature?: string;
  ttl: number;
}

export interface BitchatMessage {
  id: string;
  sender: string;
  content: string;
  timestamp: number;
  isRelay: boolean;
  originalSender?: string;
  isPrivate: boolean;
  recipientNickname?: string;
  senderPeerID: string;
  mentions: string[];
  channel?: string;
  encryptedContent?: string;
  isEncrypted: boolean;
  deliveryStatus: string;
}

export interface DeliveryAck {
  messageID: string;
  recipientID: string;
  nickname: string;
  hopCount: number;
  timestamp: number;
}

export interface ReadReceipt {
  messageID: string;
  readerID: string;
  timestamp: number;
}

export type DeliveryStatus = 'PENDING' | 'DELIVERED' | 'READ';
