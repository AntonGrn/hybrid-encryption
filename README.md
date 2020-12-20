# hybrid-encryption
Distribute symmetric AES keys using asymmetric encryption (PKI)

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
```

Distribute symmetric keys and pass initial message from client to server [1-11]


[1-2] SERVER
Preconditions: 
- Client has requested connection with server (e.g. Socket TCP handshake)
- Client is launched on new client thread (if multi-client server)
- Client thread holds unique instance of ServerCryptography
```java
serverCryptography.generateAsymmericKeyPair()
byte[] publicKey = serverCryptography.getPublicKeyAsByteArray()
```
**Write to client:** byte[] publicKey

[3-7] CLIENT

**Read from server:** byte[] publicKey
```java
clientCryptography.setServersPublicKey(publicKey);
clientCryptography.generateSymmetricKeys();
byte[] encryptedMsg = clientCryptography.createInitialMsg("Hello World!");
```
**Write to server:** byte[] encryptedMsg

[8-11] SERVER

**Read from client:** byte[] encryptedMsg
```java
String intialMsg = processInitialInputMsg(encryptedMsg);
```

Symmetric encyption (12->)
Encrypted traffic may now flow asynchronous in full-duplex

```java
SERVER
Read from client: byte[] encryptedInput;
String decrytpedMsg = serverCryptography.symmetricDecryption(encryptedInput);
byte[] encryptedOutput = serverCryptography.symmetricEncryption("My message");
Write to client: byte[] encryptedOutput

```
