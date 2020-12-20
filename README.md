# hybrid-encryption
Distribute symmetric AES keys using asymmetric encryption (PKI)

GOAL:     Use symmetric cryptography for the client-server communication.
PROBLEM:  Distribute symmetric (secret) AES key in secure way.
SOLUTION: Distribute symmetric keys using asymmetric cryptography (RSA).
          = Hybrid encryption
          
**CLIENT**

1. Client requests Socket connection with server.

**SERVER**

2. Server sends its public key (asymmetric) to client.

**CLIENT**

3. Client generates AES key, IV, MAC-key (for symmetric cryptography).
4. Client encrypts AES key, IV, MAC-key using server's public key (asymmetric encryption).
5. Client encrypts login data (payload) with AES-key, IV (symmetric encryption).
6. Client generates MAC of the payload.
7. Client sends encrypted its first msg: AES-key, IV, MAC-key, payload, MAC to server.


APPROACH:
``` 
CLIENT
1. Client requests Socket connection with server.
SERVER
2. Server sends its public key (asymmetric) to client.
CLIENT
3. Client generates AES key, IV, MAC-key (for symmetric cryptography).
4. Client encrypts AES key, IV, MAC-key using server's public key (asymmetric encryption).
5. Client encrypts login data (payload) with AES-key, IV (symmetric encryption).
6. Client generates MAC of the payload.
7. Client sends encrypted its first msg: AES-key, IV, MAC-key, payload, MAC to server.

 ____________________________________________________________________
|                         |                                          |
|   Shared secrets for    |       Payload:                           |
| Symmetric cryptography  |       Initial message                    |
|_________________________|__________________________________________|
|            |            |            |            |                |
|  AES-key   |  MAC-key   |    IV      |    MAC     |    Message     |
|  128 bit   |  128 bit   |  128 bit   |  128 bit   | Variable size  |
|____________|____________|____________|____________|________________|
|                         |            |                             |
|     Encrypted:          | Plaintext  |        Encrypted:           |
|    Asymmetric (RSA)     |            |      Symmetric (AES)        |
|_________________________|____________|_____________________________|
Initial message sent from client to server

SERVER
8. Server decrypts AES-key, IV, MAC-key with Server's private key (asymmetric decryption).
9. Server assigns the symmetric key variables (AES-key, IV, MAC-key).
10. Server decrypts the payload using the symmetric key variables (symmetric decryption).
11. Server verifies MAC.
ENCRYPTED CHANNEL ESTABLISHED
12. If verification is successful; the continuous communication between client and server
within the session will use symmetric cryptography (symmetric keys has been distributed).

 __________________________________________
|            |            |                  |
|    IV      |    MAC     |     Message      |
|  128 bit   |  128 bit   |  Variable size   |
|____________|____________|__________________|
|            |                               |
| Plaintext  |  Encrypted: Symmetric (AES)   |
|____________|_______________________________|
Encrypted messages after symmetric keys has been distributed
(For CBC: IV can securely be sent in plaintext)
```


Distribute symmetric keys and pass initial message from client to server [1-11]

**[1-2] SERVER**

Preconditions: 
- Client has requested connection with server (e.g. Socket TCP handshake)
- Client is launched on new client thread (if multi-client server)
- Client thread holds unique instance of ServerCryptography
  - Allowing unique encryption for each client session.
```java
serverCryptography.generateAsymmericKeyPair()
byte[] publicKey = serverCryptography.getPublicKeyAsByteArray()

//Write to client: byte[] publicKey
```

**[3-7] CLIENT**
```java
//Read from server: byte[] publicKey

clientCryptography.setServersPublicKey(publicKey);
clientCryptography.generateSymmetricKeys();
byte[] encryptedMsg = clientCryptography.createInitialMsg("Hello World!");

//Write to server: byte[] encryptedMsg
```

**[8-11] SERVER**


```java
//Read from client: byte[] encryptedMsg

String intialMsg = processInitialInputMsg(encryptedMsg);
```

**[12] Symmetric encyption**
Encrypted traffic may now flow asynchronous in full-duplex

```java
SERVER
Read from client: byte[] encryptedInput;
String decrytpedMsg = serverCryptography.symmetricDecryption(encryptedInput);
byte[] encryptedOutput = serverCryptography.symmetricEncryption("My message");
Write to client: byte[] encryptedOutput

```
