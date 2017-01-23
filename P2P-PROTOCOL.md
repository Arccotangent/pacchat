# PacChat P2P Protocol Documentation

PacChat P2P communicates over TCP port 14581. The protocol is still in development and will be documented here as development progresses.

The experimental P2P protocol is very messy and most certainly has serious bugs. It might not work at all in its current state.

## 101 ping

Currently unused by PacChat, but the functionality is still built in. You can telnet to a PacChat server and send `101 ping` to it, it will respond with `102 pong`

## 102 pong

Ping response message.

## 103 version

Currently unused by PacChat, but the functionality is still built in. You can telnet to a PacChat server and send '103 version' to it, the server will respond with its version.

## 110 disconnecting (previously 103 disconnecting)

Sent from peer to peer, indicates that the sender will be disconnecting the receiver. The connection is then closed.

## 200 message

Multi-line message sent from peer to peer.

Contains 4 lines below the header

Line 1 = Origin key fingerprint

Line 2 = Destination key fingerprint

Line 3 = Timestamp in milliseconds

Line 4 = Base 64 encoded message, to be passed to the regular server.

## 300 getaddr

Sent by a client to a server, the server will then respond with `301 peers`

## 301 peers

Multi-line message sent by a server to a client in response to `300 getaddr`

Contains a minimum of 2 lines below the header

Line 1 = amount of peers, maximum not yet determined

Lines 2 and on = peers

## 302 no peers

Sent by a server to a client in response to `300 getaddr`

This is sent if the server has no peers other than the client.

## 401 invalid p2p transmission header

Sent if the sender has sent an invalid message header.
