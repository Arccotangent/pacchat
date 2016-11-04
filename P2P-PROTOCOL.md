# PacChat P2P Protocol Documentation

PacChat P2P is still in development. The protocol will be documented here as development progresses.

## 101 ping

Currently unused by PacChat, but the functionality is still built in. You can telnet to a PacChat server and send `101 ping` to it, it will respond with `102 pong`

## 102 pong

Ping response message.

## 300 getaddr

Sent by a client to a server, the server will then respond with `301 peers`

## 301 peers

Multi-line message sent by a server to a client in response to `300 getaddr`

Contains a minimum of 2 lines below the header

Line 1 = amount of peers, maximum not yet determined

Lines 2 and on = peers
