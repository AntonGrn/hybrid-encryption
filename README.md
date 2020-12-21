# Hybrid Encryption
- Symmetric encryption (AES with CBC) and message authentication (MAC).
- Unique secret keys are distributed using asymmetric encryption (RSA) at the initialization of each TCP session

**GOAL:**    Use symmetric cryptography (AES) for the client-server communication.</br>
**PROBLEM:**  Distribute symmetric key (AES) in secure way.</br>
**SOLUTION:** Distribute symmetric keys (AES) using asymmetric cryptography (RSA).</br>

## APPROACH:
          
**CLIENT**</br>
**1.** Client requests connection with server (e.g. TCP handshake).</br>
**SERVER**</br>
**2.** Server sends its public key (asymmetric) to client.</br>
**CLIENT**</br>
**3.** Client generates AES key, IV, MAC-key (for symmetric cryptography).</br>
**4.** Client encrypts AES key, IV, MAC-key using server's public key (asymmetric encryption).</br>
**5.** Client encrypts initial message (payload) with AES-key, IV (symmetric encryption).</br>
**6.** Client generates MAC of the payload.</br>
**7.** Client sends encrypted its first msg: AES-key, IV, MAC-key, payload, MAC to server.</br>
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
**8.** Server decrypts AES-key, IV, MAC-key with Server's private key (asymmetric decryption).</br>
**9.** Server assigns the symmetric key variables (AES-key, IV, MAC-key).</br>
**10.** Server decrypts the payload using the symmetric key variables (symmetric decryption).</br>
**11.** Server verifies MAC.</br>
**ENCRYPTED CHANNEL ESTABLISHED (symmetric keys distributed and verified)**</br>
**12.** The continuous communication between client and server within the session will use symmetric cryptography (AES).</br>
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

In relation to above description:</br>
[1-11] Distribute symmetric keys and pass initial message from client to server</br>
[12] Symmetric encryption

**[1-2] SERVER**

Preconditions: 
- Client has requested connection with server (e.g. Socket TCP handshake)
- Client is launched on new client thread (if multi-client server)
- Client thread holds unique instance of ServerCryptography
  - Allowing unique encryption credentials (RSA & AES) for each client session.
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

String intialMsg = serverCryptography.processInitialInputMsg(encryptedMsg);
```

**[12] Symmetric encyption**</br>
Encrypted traffic may now flow asynchronous in full-duplex

```java
SERVER
Read from client: byte[] encryptedInput;
String decrytpedMsg = serverCryptography.symmetricDecryption(encryptedInput);
byte[] encryptedOutput = serverCryptography.symmetricEncryption("My message");
Write to client: byte[] encryptedOutput

```
