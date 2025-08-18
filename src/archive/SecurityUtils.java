import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    protected static SecretKeySpec secretKey;

    // Derive Secret key from password 
    public static void KeyManager(String masterPassword) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = Arrays.copyOf(sha.digest(masterPassword.getBytes("UTF-8")), 16); // 128-bit key
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        
    }

    // AES uses 16-byte blocks, so IV must be 16 bytes
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Encrypts plain text using AES with the provided key.
     *
     * @param plainText The password to encrypt.
     * @param keyBytes  16/24/32 byte key derived from user master password.
     * @return Encrypted data (Base64 encoded ciphertext + Base64 IV).
     */
    public static EncryptedData encrypt(String plainText, byte[] keyBytes) throws Exception {
        // Generate random IV
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Create AES key
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        // Init Cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        // Encrypt
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Encode to Base64 for storage
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        return new EncryptedData(encryptedBase64, ivBase64);
    }

    /**
     * Decrypts ciphertext using AES with the provided key.
     */
    public static String decrypt(String encryptedBase64, String ivBase64, byte[] keyBytes) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, "UTF-8");
    }

    // Helper class to store encryption result
    public static class EncryptedData {
        public final String ciphertext;
        public final String iv;

        public EncryptedData(String ciphertext, String iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
    }
}
