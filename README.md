# hybrid-encryption
Distribute symmetric AES keys using asymmetric encryption (PKI)

Distribute symmetric keys and pass initial message from client to server [1-11]

´´´java
SERVER
[On new client thread]
Precondition: Client has requested connection with server (e.g. Socket TCP handshake)

ServerCryptography serverCryptography = new ServerCryptography();
serverCryptography.generateAsymmericKeyPair()
byte[] publicKey = serverCryptography.getPublicKeyAsByteArray()

Write to client: byte[] publicKey

CLIENT

Read from server: byte[] publicKey

ClientCryptograhy clientCryptography = new ClientCryptograhy();
clientCryptography.setServersPublicKey(publicKey);
clientCryptography.generateSymmetricKeys();
String initialOutputMsg = "Hello World!"; // E.g. login credentials
byte[] encryptedMsg = clientCryptography.createInitalMessage(initialOutputMsg);

Write to server: byte[] encryptedMsg

SERVER

Read from client: byte[] encryptedInput

String intialMsg = processInitialClientInput(encryptedInput);



´´´

Symmetric encyption (12->)
