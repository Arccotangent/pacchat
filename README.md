# PacChat

PacChat is a direct P2P chat application that will encrypt and sign all messages you send, and verify the authenticity of all messages you receive.

This program is still in beta.

The P2P network is in beta and extremely buggy. It might not work at all in its present state.

## Pros and Cons

### Pros

* Direct communication to the recipient's IP address, no middleman
* 100% private chats, messages are not logged or sent to any third party
* Messages are encrypted with RSA-4096 and AES-128
* Messages are signed with SHA-512 and RSA-4096, and message authenticity is verified on arrival

### Cons

* Port forwarding required on both ends, however this should be taken care of automatically by PacChat through UPNP
* Port forwarding means that PacChat working on any public, school, or workplace network is highly unlikely

## Downloading & Building

```
git clone https://github.com/Arccotangent/pacchat.git
cd pacchat
```

Build on Unix-like systems: `./gradlew build`

Build on Windows: `gradlew build`

To launch from source root: `java -jar build/libs/pacchat.jar`
