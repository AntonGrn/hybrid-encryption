# Hybrid Encryption
- Symmetric encryption (AES with CBC) and message authentication (MAC).
- Symmetric keys distributed using asymmetric encryption (RSA).
- Unique keys generated for each new TCP session.

## MOTIVATION:

**GOAL:**    Use symmetric cryptography (AES) for client-server communication.</br>
**PROBLEM:**  Distribute symmetric key (AES) in a secure way.</br>
**SOLUTION:** Distribute symmetric keys (AES) using asymmetric cryptography (RSA).</br>

## APPROACH (STEPS):
          
**CLIENT**</br>
**1.** Client requests connection with server (e.g. TCP handshake).</br>
**SERVER**</br>
**2.** Server sends public key to client (asymmetric).</br>
**CLIENT**</br>
**3.** Client generates AES key, IV and MAC-key (for symmetric encryption).</br>
**4.** Client encrypts AES key and MAC-key using server's public key (asymmetric encryption).</br>
**5.** Client encrypts initial output message (payload) using AES-key and IV (symmetric encryption).</br>
**6.** Client generates MAC of the payload.</br>
**7.** Client sends first message to server: AES-key, MAC-key, IV, MAC, message.</br>
```
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
[7] Initial message sent from client to server
``` 
**SERVER**</br>
**8.** Server decrypts AES-key and MAC-key with Server's private key (asymmetric decryption).</br>
**9.** Server assigns the symmetric key variables (AES-key, MAC-key, IV).</br>
**10.** Server decrypts the payload using the symmetric key variables (symmetric decryption).</br>
**11.** Server verifies MAC.</br>
**ENCRYPTED CHANNEL ESTABLISHED (symmetric keys distributed)**</br>
**12.** The continuous communication between client and server will use symmetric cryptography (AES).</br>
``` 
 ____________________________________________
|            |            |                  |
|    IV      |    MAC     |     Message      |
|  128 bit   |  128 bit   |  Variable size   |
|____________|____________|__________________|
|            |                               |
| Plaintext  |  Encrypted: Symmetric (AES)   |
|____________|_______________________________|
[12] Encrypted messages after symmetric keys has been distributed
(For CBC: IV can securely be sent in plaintext)
```
## USER GUIDE:

Chronological operations in relation to above notations.

**[1-2] SERVER**

Preconditions: 
- Client has requested connection with server (e.g. Socket TCP handshake).
- Each client instance (on server) is mapped to, or holds, unique instance of ```ServerCryptography```.
  - Allows unique encryption credentials (RSA & AES) for each client session.
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

String intialMsg = serverCryptography.processInitialMsg(encryptedMsg);
```

**[12] Symmetric cryptography**</br>
AES encrypted traffic may now flow asynchronous in full-duplex, using the following methods:

```java
SERVER
// Read from client: byte[] encryptedInput;
String decrytpedInput = serverCryptography.symmetricDecryption(encryptedInput);
byte[] encryptedOutput = serverCryptography.symmetricEncryption("My message");
// Write to client: byte[] encryptedOutput
[...]

CLIENT
// Read from server: byte[] encryptedInput;
String decrytpedInput = clientCryptography.symmetricDecryption(encryptedInput);
byte[] encryptedOutput = clientCryptography.symmetricEncryption("My message");
// Write to server: byte[] encryptedOutput
[...]

```

## COMPLEMENT WITH:
- Key store
- Certificate and digitial signatures
