# API Reference

## BitchatPacket

Represents a packet in the bitchat protocol with the following fields:
- `version` (number): Protocol version (e.g., 1).
- `type` (number): Packet type (e.g., message, ack).
- `senderID` (string): 8-byte sender identifier.
- `recipientID` (string): 8-byte recipient identifier or broadcast.
- `timestamp` (number): Packet timestamp in milliseconds.
- `payload` (string): Encoded payload data.
- `signature` (string, optional): 64-byte signature for verification.
- `ttl` (number): Time to live for routing.

## BitchatMessage

Represents a message with the following fields:
- `id` (string): Unique message ID (UUID).
- `sender` (string): Message sender.
- `content` (string): Message content.
- `timestamp` (number): Message timestamp in milliseconds.
- `isRelay` (boolean): Indicates if the message is relayed.
- `originalSender` (string, optional): Original sender if relayed.
- `isPrivate` (boolean): Indicates if the message is private.
- `recipientNickname` (string, optional): Recipient nickname for private messages.
- `senderPeerID` (string): Sender's peer ID.
- `mentions` (string[]): List of mentioned users.
- `channel` (string, optional): Channel name (e.g., `#channel`).
- `encryptedContent` (string, optional): Encrypted content if applicable.
- `isEncrypted` (boolean): Indicates if the content is encrypted.
- `deliveryStatus` (string): Status (e.g., `PENDING`, `DELIVERED`, `READ`).

## DeliveryAck

Represents a delivery acknowledgment with:
- `messageID` (string): ID of the acknowledged message.
- `recipientID` (string): Recipient identifier.
- `nickname` (string): Recipient nickname.
- `hopCount` (number): Number of hops taken.
- `timestamp` (number): Acknowledgment timestamp in milliseconds.

## ReadReceipt

Represents a read receipt with:
- `messageID` (string): ID of the read message.
- `readerID` (string): Identifier of the reader.
- `timestamp` (number): Read timestamp in milliseconds.

## Methods

### startAdvertising(peerID: string)
Starts advertising the device with the specified peer ID.

- **Parameters**:
  - `peerID` (string): 8-byte peer identifier.
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function start() {
    await Bitchat.startAdvertising('myPeerID');
    console.log('Advertising started');
  }
  start();
  ```

### scanPeers()
Scans for nearby peers and returns their IDs.

- **Returns**: Promise<string[]>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function scan() {
    const peers = await Bitchat.scanPeers();
    console.log('Found peers:', peers);
  }
  scan();
  ```

### sendMessage(message: BitchatMessage, recipient?: string)
Sends a message to a recipient or broadcasts it.

- **Parameters**:
  - `message` (BitchatMessage): Message object.
  - `recipient` (string, optional): Recipient ID for private messages.
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function send() {
    const message = { id: 'msg1', sender: 'user', content: 'Hi!', timestamp: Date.now(), isPrivate: false, senderPeerID: 'myPeerID', mentions: [], isEncrypted: false, deliveryStatus: 'PENDING' };
    await Bitchat.sendMessage(message);
    console.log('Message sent');
  }
  send();
  ```

### sendEncryptedChannelMessage(message: BitchatMessage, channel: string)
Sends an encrypted message to a channel.

- **Parameters**:
  - `message` (BitchatMessage): Message object with encrypted content.
  - `channel` (string): Channel name (e.g., `#secureChannel`).
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function sendEncrypted() {
    const message = { id: 'msg2', sender: 'user', content: '', timestamp: Date.now(), isEncrypted: true, encryptedContent: 'encryptedData', channel: '#secureChannel', senderPeerID: 'myPeerID', mentions: [], deliveryStatus: 'PENDING' };
    await Bitchat.sendEncryptedChannelMessage(message, '#secureChannel');
    console.log('Encrypted message sent');
  }
  sendEncrypted();
  ```

### onMessageReceived(callback: (message: BitchatMessage) => void)
Sets a callback for receiving messages.

- **Parameters**:
  - `callback` (function): Callback function to handle received messages.
- **Returns**: void
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  Bitchat.onMessageReceived((message) => {
    console.log('Received message:', message.content);
  });
  ```

### joinChannel(channel: string, password?: string)
Joins a channel with an optional password.

- **Parameters**:
  - `channel` (string): Channel name (e.g., `#secureChannel`).
  - `password` (string, optional): Password for protected channels.
- **Returns**: Promise<boolean>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function join() {
    const success = await Bitchat.joinChannel('#secureChannel', 'password123');
    if (success) console.log('Joined channel');
  }
  join();
  ```

### setChannelPassword(channel: string, password: string)
Sets a password for a channel.

- **Parameters**:
  - `channel` (string): Channel name.
  - `password` (string): New password.
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function secureChannel() {
    await Bitchat.setChannelPassword('#secureChannel', 'newPassword');
    console.log('Channel secured');
  }
  secureChannel();
  ```

### removeChannelPassword(channel: string)
Removes the password from a channel.

- **Parameters**:
  - `channel` (string): Channel name.
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function unsecureChannel() {
    await Bitchat.removeChannelPassword('#secureChannel');
    console.log('Channel unsecured');
  }
  unsecureChannel();
  ```

### transferChannelOwnership(channel: string, newOwnerID: string)
Transfers ownership of a channel.

- **Parameters**:
  - `channel` (string): Channel name.
  - `newOwnerID` (string): New owner ID.
- **Returns**: Promise<void>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function transfer() {
    await Bitchat.transferChannelOwnership('#secureChannel', 'newOwnerID');
    console.log('Ownership transferred');
  }
  transfer();
  ```

### decryptChannelMessage(encryptedContent: string, channel: string)
Decrypts a channel message.

- **Parameters**:
  - `encryptedContent` (string): Encrypted content to decrypt.
  - `channel` (string): Channel name for key derivation.
- **Returns**: Promise<string>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function decrypt() {
    const decrypted = await Bitchat.decryptChannelMessage('encryptedData', '#secureChannel');
    console.log('Decrypted content:', decrypted);
  }
  decrypt();
  ```

### isFavorite(fingerprint: string)
Checks if a peer is marked as favorite.

- **Parameters**:
  - `fingerprint` (string): Peer fingerprint.
- **Returns**: Promise<boolean>
- **Example**:
  ```js
  import Bitchat from 'react-native-bitchat';

  async function checkFavorite() {
    const isFav = await Bitchat.isFavorite('peerFingerprint');
    console.log('Is favorite:', isFav);
  }
  checkFavorite();
  ```