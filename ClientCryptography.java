import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class ClientCryptography {

    // ASYMMETRIC KEYS
    private PublicKey serversPublicKey;
    // SYMMETRIC KEYS (shared secret)
    private SecretKey AES_key;   // Key to encrypt/decrypt
    private SecretKey  MAC_key;  // Key to create MAC
    // Note: Keys should be stored in a secure file (key store)

    public void setServersPublicKey(byte[] receivedPublicKey) throws Exception {
        serversPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(receivedPublicKey));
    }

    public void generateSymmetricKeys() throws Exception{
        // AES key:
        KeyGenerator keyGenAES = KeyGenerator.getInstance("AES");
        keyGenAES.init(128, new SecureRandom()); // increase?
        AES_key = keyGenAES.generateKey();
        // MAC key:
        KeyGenerator keyGenMAC = KeyGenerator.getInstance("HMACMD5"); // AES?
        keyGenMAC.init(128, new SecureRandom()); // increase?
        MAC_key = keyGenMAC.generateKey();
    }

    public byte[] createInitialMsg(String initialOutputMsg) throws Exception {
        byte[] enc_AES_key = asymmetricEncryptionRSA(AES_key.getEncoded());
        byte[] enc_MAC_key = asymmetricEncryptionRSA(MAC_key.getEncoded());
        byte[] enc_msg_AES = symmetricEncryption(initialOutputMsg);

        byte[] secretKeys = concatByteArrays(enc_AES_key, enc_MAC_key);

        // AES key (encrypted:RSA), MAC key (encrypted:RSA), IV (plain text), MAC (encrypted: AES), message (encrypted: AES)
        return concatByteArrays(secretKeys, enc_msg_AES);
    }

    private byte[] asymmetricEncryptionRSA(byte[] plainText) throws Exception {
        // Create a Cipher instance for cryptography operation. State the algorithm to be used.
        Cipher decryptRSA = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        // Initialize the Cipher with: operation mode and key.
        decryptRSA.init(Cipher.ENCRYPT_MODE, serversPublicKey);
        // Return the encrypted data.
        return decryptRSA.doFinal(plainText); // byte array
    }

    // ================= Symmetric cryptography: Same for both communicating parties ======================

    public String symmetricDecryption(byte[] encryptedData) throws Exception {
        if (encryptedData.length < 40 || encryptedData.length > 1000) {
            throw new Exception("Client input of suspicious length: " + encryptedData.length);
        }
        // Split IV (plaintext) from encrypted MAC & message (ciphertext)
        byte[] IV = Arrays.copyOfRange(encryptedData, 0, 16); // 16 bytes = 128 bits ;
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 16, encryptedData.length); // MAC + message

        // Create an Initial Vector instance from byte array.
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        // Create a Cipher instance and pass the algorithm to be used. CBC because we use IV
        Cipher decryptAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // Initialize the Cipher with mode of operation, key and IV.
        decryptAES.init(Cipher.DECRYPT_MODE, AES_key, ivSpec);
        // Decrypt into byte array
        byte[] decryptedData = decryptAES.doFinal(cipherText);

        // Split decrypted MAC from decrypted message
        byte[] referenceMAC = Arrays.copyOfRange(decryptedData,0, 16); // 128 bit key
        byte[] decryptedMessage = Arrays.copyOfRange(decryptedData, 16, decryptedData.length);
        // Verify message authentication
        if (!verifyMAC(referenceMAC, decryptedMessage)) {
            throw new Exception("MAC did not match");
        }
        return new String(decryptedMessage); //plainText
    }

    public byte[] symmetricEncryption(String message) throws Exception { // returns IV + encrypted MAC + encrypted message (as byte array)
        byte[] messageByteArray = message.getBytes();
        byte[] MAC = generateMAC(messageByteArray);
        byte[] IV = generateRandomIV();

        // Concatenate MAC and message (in plain text)
        byte[] bufferToEncrypt = concatByteArrays(MAC, messageByteArray);
        // Encrypt MAC + message
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        Cipher encryptAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptAES.init(Cipher.ENCRYPT_MODE, AES_key, ivSpec);
        byte[] cipherText = encryptAES.doFinal(bufferToEncrypt);
        // Append IV (in plaintext) at beginning of cipher text
        return concatByteArrays(IV, cipherText);
    }

    private byte[] concatByteArrays(byte[] firstArray, byte[] secondArray) {
        byte[] buffer = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, buffer, 0, firstArray.length);
        System.arraycopy(secondArray, 0, buffer, firstArray.length, secondArray.length);
        return buffer;
    }

    private byte[] generateRandomIV() {
        // Initial Vector (preventing equal plaintext blocks mapping to equal cipher text blocks. A "scrambler")
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] IV = new byte[16]; // block size ??
        randomSecureRandom.nextBytes(IV);
        return IV;
    }

    private boolean verifyMAC(byte[] referenceMAC, byte[] decryptedData)  {
        byte[] computedMAC = generateMAC(decryptedData);
        return (Arrays.equals(referenceMAC, computedMAC));
    }

    private byte[] generateMAC(byte[] message) {
        try {
            Mac mac = Mac.getInstance("HMACMD5");
            mac.init(MAC_key);
            return mac.doFinal(message); // MAC as byte array
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

}
