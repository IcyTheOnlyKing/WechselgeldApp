package htl.steyr.wechselgeldapp.Utilities.Security;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SecureData {

    // Private constructor to prevent instantiation of this utility class
    private SecureData() {}

    /**
     * Hashes input data using SHA-256 algorithm and returns the hex representation.
     * This method is typically used for hashing usernames, emails, or other non-password data.
     *
     * @param data The input string what will be hashed
     * @return The SHA-256 hashed hex string of the input data
     */
    public static String hashDataViaSHA(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hashes a plaintext password using BCrypt with a cost factor of 12.
     * BCrypt is used here to securely hash passwords before storing them.
     *
     * @param password The plaintext password to hash
     * @return The BCrypt hashed password string
     */
    public static String hashPasswordViaBCrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plaintext password against a previously hashed password using BCrypt.
     *
     * @param plainPassword The plaintext password entered by the user
     * @param hashedPassword The hashed password stored in the database
     * @return True if the password matches the hash, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

}