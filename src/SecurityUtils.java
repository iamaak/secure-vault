import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    // ===== Password hashing (for master password verification) =====
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes("UTF-8"));
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    // ===== Session AES key management (derived from master password for the session) =====
    // This is a minimal approach for your current project; you should upgrade to PBKDF2 later.
    private static SecretKeySpec sessionKey = null;

    /** Derive and store a session AES key (128-bit) from the given master password. */
    public static void initSessionKeyFromPassword(String masterPassword) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = Arrays.copyOf(sha.digest(masterPassword.getBytes("UTF-8")), 16); // 128-bit
        sessionKey = new SecretKeySpec(keyBytes, "AES");
        // Best-effort cleanup
        Arrays.fill(keyBytes, (byte)0);
    }

    /** Wipe the in-memory session key (call on logout). */
    public static void clearSessionKey() {
        sessionKey = null;
    }

    /** Return whether a session key is currently initialized. */
    public static boolean hasSessionKey() {
        return sessionKey != null;
    }

    // ===== AES-CBC encryption utilities =====
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /** Container for encryption result */
    public static class EncryptedData {
        public final String ciphertextBase64;
        public final String ivBase64;
        public EncryptedData(String ciphertextBase64, String ivBase64) {
            this.ciphertextBase64 = ciphertextBase64;
            this.ivBase64 = ivBase64;
        }
    }

    /** Encrypt plaintext using the current session key. Returns Base64 ciphertext and IV. */
    public static EncryptedData encrypt(String plainText) throws Exception {
        if (sessionKey == null) throw new IllegalStateException("Session key not initialized");
        // Generate random 16-byte IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        // Best-effort cleanup
        Arrays.fill(iv, (byte)0);

        return new EncryptedData(encryptedBase64, ivBase64);
    }

    /** Decrypt Base64 ciphertext using the current session key and the given Base64 IV. */
    public static String decrypt(String encryptedBase64, String ivBase64) throws Exception {
        if (sessionKey == null) throw new IllegalStateException("Session key not initialized");

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Best-effort cleanup
        Arrays.fill(iv, (byte)0);

        return new String(decryptedBytes, "UTF-8");
    }
}