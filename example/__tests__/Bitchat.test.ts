import Bitchat from 'react-native-bitchat';
import type { BitchatMessage, DeliveryStatus } from '../../src/types'; // Type-only import

// Define types for ACK, Receipt, and Status Update based on your needs
interface DeliveryAck {
  messageID: string;
  recipientID: string;
  nickname: string;
  hopCount: number;
  timestamp: number;
}

interface ReadReceipt {
  messageID: string;
  readerID: string;
  timestamp: number;
}

interface DeliveryStatusUpdate {
  messageID: string;
  status: DeliveryStatus;
}

describe('Bitchat Integration Tests', () => {
  let messageCallback: (message: BitchatMessage) => void;
  let peerConnectedCallback: (peer: string) => void;
  let peerDisconnectedCallback: (peer: string) => void;
  let deliveryAckCallback: (ack: DeliveryAck) => void;
  let readReceiptCallback: (receipt: ReadReceipt) => void;
  let deliveryStatusUpdateCallback: (status: DeliveryStatusUpdate) => void;

  beforeAll(async () => {
    await Bitchat.startAdvertising('peer12345');
    Bitchat.onMessageReceived((message: BitchatMessage) => messageCallback?.(message));
    Bitchat.onPeerConnected((peer: string) => peerConnectedCallback?.(peer));
    Bitchat.onPeerDisconnected((peer: string) => peerDisconnectedCallback?.(peer));
    Bitchat.onDeliveryAck((ack: DeliveryAck) => deliveryAckCallback?.(ack));
    Bitchat.onReadReceipt((receipt: ReadReceipt) => readReceiptCallback?.(receipt));
    Bitchat.onDeliveryStatusUpdate((status: DeliveryStatusUpdate) => deliveryStatusUpdateCallback?.(status));
  });

  beforeEach(() => {
    messageCallback = jest.fn();
    peerConnectedCallback = jest.fn();
    peerDisconnectedCallback = jest.fn();
    deliveryAckCallback = jest.fn();
    readReceiptCallback = jest.fn();
    deliveryStatusUpdateCallback = jest.fn();
  });

  afterAll(async () => {
    await Bitchat.removeChannelPassword('#testChannel'); // Cleanup
  });

  it('sends and receives a message', async () => {
    const message: BitchatMessage = {
      id: 'msg1',
      sender: 'user1',
      content: 'Hello, world!',
      timestamp: Date.now(),
      isRelay: false,
      isPrivate: false,
      senderPeerID: 'peer12345',
      mentions: [],
      isEncrypted: false,
      deliveryStatus: 'PENDING',
    };
    await Bitchat.sendMessage(message, null);
    await new Promise(resolve => setTimeout(resolve, 1000)); // Wait for receive
    expect(messageCallback).toHaveBeenCalledWith(expect.objectContaining({ content: 'Hello, world!' }));
  });

  it('joins and manages a channel', async () => {
    await Bitchat.joinChannel('#testChannel', 'password123');
    expect(await Bitchat.getStatus('#testChannel')).toBe('JOINED'); // Assume getStatus extension
    await Bitchat.setChannelPassword('#testChannel', 'newPassword');
    await Bitchat.removeChannelPassword('#testChannel');
    expect(await Bitchat.getStatus('#testChannel')).toBe('JOINED');
  });

  it('sends encrypted channel message', async () => {
    const encryptedMessage: BitchatMessage = {
      id: 'msg2',
      sender: 'user1',
      content: 'Secure message',
      timestamp: Date.now(),
      isRelay: false,
      isPrivate: false,
      senderPeerID: 'peer12345',
      mentions: [],
      isEncrypted: true,
      encryptedContent: 'encryptedData', // Assume pre-encrypted
      deliveryStatus: 'PENDING',
    };
    await Bitchat.joinChannel('#secureChannel', 'password123');
    await Bitchat.sendEncryptedChannelMessage(encryptedMessage, '#secureChannel');
    await new Promise(resolve => setTimeout(resolve, 1000));
    const decrypted = await Bitchat.decryptChannelMessage('encryptedData', '#secureChannel');
    expect(decrypted).toBe('Secure message');
  });

  it('emits peer connection events', async () => {
    await new Promise(resolve => setTimeout(resolve, 500)); // Wait for potential event
    expect(peerConnectedCallback).toHaveBeenCalledWith(expect.any(String));
  });

  it('emits peer disconnection events', async () => {
    await new Promise(resolve => setTimeout(resolve, 500));
    expect(peerDisconnectedCallback).toHaveBeenCalledWith(expect.any(String));
  });

  it('emits delivery ACK', async () => {
    const message: BitchatMessage = { id: 'msg3', sender: 'user1', content: 'Ack test', timestamp: Date.now(), isRelay: false, isPrivate: false, senderPeerID: 'peer12345', mentions: [], isEncrypted: false, deliveryStatus: 'PENDING' };
    await Bitchat.sendMessage(message, 'peer54321');
    await new Promise(resolve => setTimeout(resolve, 1000));
    expect(deliveryAckCallback).toHaveBeenCalled();
  });

  it('emits read receipt', async () => {
    const message: BitchatMessage = { id: 'msg4', sender: 'user1', content: 'Read test', timestamp: Date.now(), isRelay: false, isPrivate: false, senderPeerID: 'peer12345', mentions: [], isEncrypted: false, deliveryStatus: 'PENDING' };
    await Bitchat.sendMessage(message, 'peer54321');
    await new Promise(resolve => setTimeout(resolve, 1000));
    expect(readReceiptCallback).toHaveBeenCalled();
  });

  it('emits delivery status update', async () => {
    const message: BitchatMessage = { id: 'msg5', sender: 'user1', content: 'Status test', timestamp: Date.now(), isRelay: false, isPrivate: false, senderPeerID: 'peer12345', mentions: [], isEncrypted: false, deliveryStatus: 'PENDING' };
    await Bitchat.sendMessage(message, 'peer54321');
    await new Promise(resolve => setTimeout(resolve, 1000));
    expect(deliveryStatusUpdateCallback).toHaveBeenCalledWith(expect.objectContaining({ status: 'DELIVERED' }));
  });
});