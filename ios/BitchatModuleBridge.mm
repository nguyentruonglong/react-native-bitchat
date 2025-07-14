#import <React/RCTBridgeModule.h>
#import "BitchatModule-Swift.h"

@interface RCT_EXTERN_MODULE(BitchatModuleBridge, NSObject)

RCT_EXTERN_METHOD(startAdvertising:(NSString *)peerID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(scanPeers:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(sendMessage:(NSDictionary *)message
                 recipient:(NSString *)recipient
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(sendEncryptedChannelMessage:(NSDictionary *)message
                 channel:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(onMessageReceived:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(onPeerConnected:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(onPeerDisconnected:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(onDeliveryAck:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(onReadReceipt:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(onDeliveryStatusUpdate:(RCTResponseSenderBlock)callback)

RCT_EXTERN_METHOD(decryptChannelMessage:(NSString *)encryptedContent
                 channel:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(isFavorite:(NSString *)fingerprint
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(joinChannel:(NSString *)channel
                 password:(NSString *)password
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setChannelPassword:(NSString *)channel
                 password:(NSString *)password
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(removeChannelPassword:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(transferChannelOwnership:(NSString *)channel
                 newOwnerID:(NSString *)newOwnerID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)

@end

@implementation BitchatModuleBridge

BitchatModule *moduleInstance;

+ (void)initializeModule {
    if (!moduleInstance) {
        moduleInstance = [[BitchatModule alloc] init];
    }
}

RCT_EXPORT_METHOD(startAdvertising:(NSString *)peerID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance startAdvertising:peerID resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(scanPeers:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance scanPeers:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(sendMessage:(NSDictionary *)message
                 recipient:(NSString *)recipient
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance sendMessage:message recipient:recipient resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(sendEncryptedChannelMessage:(NSDictionary *)message
                 channel:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance sendEncryptedChannelMessage:message channel:channel resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(onMessageReceived:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onMessageReceived:callback];
}

RCT_EXPORT_METHOD(onPeerConnected:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onPeerConnected:callback];
}

RCT_EXPORT_METHOD(onPeerDisconnected:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onPeerDisconnected:callback];
}

RCT_EXPORT_METHOD(onDeliveryAck:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onDeliveryAck:callback];
}

RCT_EXPORT_METHOD(onReadReceipt:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onReadReceipt:callback];
}

RCT_EXPORT_METHOD(onDeliveryStatusUpdate:(RCTResponseSenderBlock)callback) {
    [self initializeModule];
    [moduleInstance onDeliveryStatusUpdate:callback];
}

RCT_EXPORT_METHOD(decryptChannelMessage:(NSString *)encryptedContent
                 channel:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance decryptChannelMessage:encryptedContent channel:channel resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(isFavorite:(NSString *)fingerprint
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance isFavorite:fingerprint resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(joinChannel:(NSString *)channel
                 password:(NSString *)password
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance joinChannel:channel password:password resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(setChannelPassword:(NSString *)channel
                 password:(NSString *)password
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance setChannelPassword:channel password:password resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(removeChannelPassword:(NSString *)channel
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance removeChannelPassword:channel resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(transferChannelOwnership:(NSString *)channel
                 newOwnerID:(NSString *)newOwnerID
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    [self initializeModule];
    [moduleInstance transferChannelOwnership:channel newOwnerID:newOwnerID resolver:resolve rejecter:reject];
}

@end