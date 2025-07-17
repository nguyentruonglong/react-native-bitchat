# React Native Bitchat Module

## Overview

The React Native Bitchat Module is designed to enable secure, decentralized communication through BLE-based mesh networking, aligning with the whitepaper's goals of privacy and peer-to-peer connectivity. It provides a robust implementation of the bitchat protocol, supporting encrypted messages, delivery tracking, and password-protected channels.

## Installation

```bash
npm install react-native-bitchat
```

## Quickstart Example

```js
import Bitchat from 'react-native-bitchat';

async function setupBitchat() {
  await Bitchat.startAdvertising('myPeerID');
  await Bitchat.joinChannel('#secureChannel', 'password123');
  await Bitchat.sendMessage({ id: 'msg1', sender: 'user', content: 'Hello!' });
}
setupBitchat();
```

## API Documentation

For detailed API methods, see [api.md](api.md).