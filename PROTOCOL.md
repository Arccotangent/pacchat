# PacChat Protocol Documentation

PacChat communicates over TCP port 14761. Most transmissions sent over the network include 1 line headers. The only exception is the transmission of a public key.

Some types of transmissions cannot be sent over the P2P network for security reasons.

## 101 ping

Currently unused by PacChat, but the functionality is still built in. You can telnet to a PacChat server and send `101 ping` to it, it will respond with `102 pong`

## 102 pong

Ping response message.

## 103 version

Currently unused by PacChat, but the functionality is still built in. You can telnet to a PacChat server (0.2-B23 and on) and send `103 version` to it, it will respond with the server version.

## 200 encrypted message

An encrypted message with 3 lines below the header.

Line 2 contains the RSA-4096 encrypted randomly generated AES-128 key.

Line 3 contains the AES-128 encrypted text message.

Line 4 contains the SHA-512/RSA-4096 signature of the encrypted AES-128 key.

## 201 message acknowledgement

An acknowledgement message sent back to a client by the receiving server.

A `201 message acknowledgement` means that the server was able to decrypt and verify the authenticity of the message without any problems.

## 202 unable to decrypt

Sent by receiving server to client if the message could not be decrypted. Verifying the authenticity is irrelevant at this point as there is no message to display.

This cannot be caused by outdated software. Updating keys is generally required.

## 203 unable to verify

Sent by receiving server to client if the message was decrypted successfully, but its authenticity could not be verified.

This cannot be caused by outdated software. Updating keys is generally recommended, but not required.

## 301 getkey

Sent by client to a server. The server will then respond with the key, and only the key. No headers shall be included in the response.

## 302 request key update

Line 2 contains the new key fingerprint as reported by client

Sent by a client to a server. Basically, this is the client asking the server (and the operator) to update their copy of the client's key. This request must be manually accepted or rejected by the server operator.

The update request is stored and kept pending until accepted or rejected.

If the server shuts down with pending updates, all pending updates are dropped and essentially rejected.

## 303 update

Sent by a server to a client when the server operator accepts a key update request.

The client will then respond with the key, and only the key. No headers shall be included in the response.

## 304 no update

Sent by a server to a client when the server operator rejects a key update request.

The client does not respond and the connection is closed.

## 305 update unavailable (unused, will probably be removed)

Sent by a server to a client when the server cannot update their key for whatever reason.

## 400 invalid transmission header

Sent by a server to a client when the client sends a message that contains no header or an invalid header.

This will most commonly be caused by outdated software, or a key being transmitted when the server is not accepting one.
