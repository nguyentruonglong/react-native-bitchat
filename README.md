# React Native Bitchat Module

A decentralized, peer-to-peer messaging library for React Native using Bluetooth Low Energy (BLE) mesh networking, enabling secure, ephemeral, and infrastructure-independent communication. This module is inspired by the design principles outlined in [Jack Dorsey’s Bitchat Whitepaper](https://github.com/permissionlesstech/bitchat/blob/main/WHITEPAPER.md), which emphasizes a protocol for private, resilient communication without centralized infrastructure, leveraging BLE for mesh networking and end-to-end encryption for security.

[![CI Status](https://github.com/nguyentruonglong/react-native-bitchat/actions/workflows/npm.yml/badge.svg)](https://github.com/nguyentruonglong/react-native-bitchat/actions)
[![npm version](https://badge.fury.io/js/react-native-bitchat.svg)](https://badge.fury.io/js/react-native-bitchat)
[![Documentation](https://img.shields.io/badge/docs-available-blue)](https://github.com/nguyentruonglong/react-native-bitchat/tree/main/docs)

## Installation

```bash
npm install react-native-bitchat
```

## Usage

The React Native Bitchat Module provides a set of methods to enable peer-to-peer messaging over BLE mesh networks. Below are detailed examples and parameter explanations to help you build your own Bitchat applications. All methods are asynchronous where applicable and return Promises, or use callbacks for event-driven data.

### Importing the Module
```bash
import Bitchat from 'react-native-bitchat';
```

### Initialization and Core Methods

#### Starting Advertising
Start advertising your device on the BLE network with a unique peer ID.
```js
async function startAdvertising() {
  try {
    await Bitchat.startAdvertising('uniquePeerID123');
    console.log('Advertising started successfully');
  } catch (error) {
    console.error('Failed to start advertising:', error.message);
  }
}
startAdvertising();
```
- **Parameters**:
  - `peerID` (String): A unique 8-byte identifier for your device (e.g., 'uniquePeerID123'). Must be unique within the network.

#### Scanning for Peers
Discover nearby peers on the network.
```js
async function scanForPeers() {
  try {
    const peers = await Bitchat.scanPeers();
    console.log('Found peers:', peers);
  } catch (error) {
    console.error('Failed to scan peers:', error.message);
  }
}
scanForPeers();
```
- **Parameters**: None.
- **Returns**: Array of strings representing peer IDs.

#### Sending a Message
Send a message to a specific recipient or broadcast it.
```js
async function sendMessage() {
  const message = {
    id: 'msg1',
    sender: 'userA',
    content: 'Hello, world!',
    timestamp: Date.now(),
    isRelay: false,
    isPrivate: false,
    senderPeerID: 'uniquePeerID123',
    mentions: [],
    deliveryStatus: 'PENDING'
  };
  try {
    await Bitchat.sendMessage(message, null); // null for broadcast, or use a peerID for private
    console.log('Message sent successfully');
  } catch (error) {
    console.error('Failed to send message:', error.message);
  }
}
sendMessage();
```
- **Parameters**:
  - `message` (Object): Contains:
    - `id` (String): Unique message ID (e.g., UUID).
    - `sender` (String): Sender's nickname.
    - `content` (String): Message text.
    - `timestamp` (Number): Unix timestamp in milliseconds.
    - `isRelay` (Boolean): Whether this is a relayed message.
    - `isPrivate` (Boolean): Whether the message is private.
    - `senderPeerID` (String): Sender's peer ID.
    - `mentions` (Array): List of mentioned user nicknames.
    - `deliveryStatus` (String): Initial status ('PENDING', 'DELIVERED', 'READ').
  - `recipient` (String, optional): Peer ID to send privately; `null` for broadcast.

#### Sending an Encrypted Channel Message
Send a message to a password-protected channel.
```js
async function sendEncryptedChannelMessage() {
  const message = {
    id: 'msg2',
    sender: 'userA',
    content: 'Secure message!',
    timestamp: Date.now(),
    isEncrypted: true,
    encryptedContent: 'encryptedDataBase64',
    channel: '#secureChannel',
    senderPeerID: 'uniquePeerID123',
    mentions: [],
    deliveryStatus: 'PENDING'
  };
  try {
    await Bitchat.sendEncryptedChannelMessage(message, '#secureChannel');
    console.log('Encrypted channel message sent');
  } catch (error) {
    console.error('Failed to send encrypted message:', error.message);
  }
}
sendEncryptedChannelMessage();
```
- **Parameters**:
  - `message` (Object): Same as above, with `isEncrypted` set to `true` and `encryptedContent` (base64-encoded string) included.
  - `channel` (String): Channel name (e.g., '#secureChannel'), must start with '#' and contain only alphanumeric characters and hyphens.

#### Joining a Channel
Join a channel, optionally with a password for protected channels.
```js
async function joinChannel() {
  try {
    await Bitchat.joinChannel('#secureChannel', 'password123');
    console.log('Joined channel successfully');
  } catch (error) {
    console.error('Failed to join channel:', error.message);
  }
}
joinChannel();
```
- **Parameters**:
  - `channel` (String): Channel name (e.g., '#secureChannel').
  - `password` (String, optional): Password for protected channels; omit or set to `null` for public channels.

#### Managing Channel Passwords
Set or remove a password for a channel (creator only).
```js
async function manageChannelPassword() {
  try {
    await Bitchat.setChannelPassword('#secureChannel', 'newPassword456');
    console.log('Password set');
    await Bitchat.removeChannelPassword('#secureChannel');
    console.log('Password removed');
  } catch (error) {
    console.error('Password management failed:', error.message);
  }
}
manageChannelPassword();
```
- **Parameters** (for `setChannelPassword`):
  - `channel` (String): Channel name.
  - `password` (String): New password.
- **Parameters** (for `removeChannelPassword`):
  - `channel` (String): Channel name.

#### Transferring Channel Ownership
Transfer ownership of a channel to another peer.
```js
async function transferOwnership() {
  try {
    await Bitchat.transferChannelOwnership('#secureChannel', 'newOwnerPeerID');
    console.log('Ownership transferred');
  } catch (error) {
    console.error('Failed to transfer ownership:', error.message);
  }
}
transferOwnership();
```
- **Parameters**:
  - `channel` (String): Channel name.
  - `newOwnerID` (String): Peer ID of the new owner.

#### Decrypting a Channel Message
Decrypt an encrypted message received from a channel.
```js
async function decryptMessage() {
  try {
    const decryptedContent = await Bitchat.decryptChannelMessage('encryptedDataBase64', '#secureChannel');
    console.log('Decrypted content:', decryptedContent);
  } catch (error) {
    console.error('Decryption failed:', error.message);
  }
}
decryptMessage();
```
- **Parameters**:
  - `encryptedContent` (String): Base64-encoded encrypted data.
  - `channel` (String): Channel name.

#### Checking Favorite Status
Check if a peer is marked as a favorite.
```js
async function checkFavorite() {
  try {
    const isFav = await Bitchat.isFavorite('peerID123');
    console.log('Is favorite:', isFav);
  } catch (error) {
    console.error('Failed to check favorite status:', error.message);
  }
}
checkFavorite();
```
- **Parameters**:
  - `fingerprint` (String): Peer ID to check.

### Event Listeners
Register callbacks to handle real-time events.
```js
function setupEventListeners() {
  Bitchat.onMessageReceived((msg) => console.log('New message:', msg.content));
  Bitchat.onPeerConnected((peerID) => console.log('Peer connected:', peerID));
  Bitchat.onPeerDisconnected((peerID) => console.log('Peer disconnected:', peerID));
  Bitchat.onDeliveryAck((ack) => console.log('Delivery ACK:', ack.messageID));
  Bitchat.onReadReceipt((receipt) => console.log('Read receipt:', receipt.messageID));
  Bitchat.onDeliveryStatusUpdate((status) => console.log('Status update:', status));
}
setupEventListeners();
```
- **Parameters** (for all `on*` methods):
  - `callback` (Function): Callback function receiving event data:
    - `onMessageReceived`: Receives a `BitchatMessage` object.
    - `onPeerConnected`/`onPeerDisconnected`: Receives a `peerID` (String).
    - `onDeliveryAck`: Receives a `DeliveryAck` object.
    - `onReadReceipt`: Receives a `ReadReceipt` object.
    - `onDeliveryStatusUpdate`: Receives a `{ messageID: string, status: string }` object.

### Error Handling
All promise-based methods reject with an error object containing a `message` property. Always use try-catch blocks to handle potential failures.

## Testing

- iOS: 
  ```bash
  yarn test:ios
  ```
- Android:
  ```bash
  yarn test:android
  ```

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests. Ensure tests pass and add new tests for new features.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details. The `license` field in `package.json` is set to "MIT", and the `LICENSE` file is included in the package root with the full MIT License text, complying with npm's requirement for open-source distribution.