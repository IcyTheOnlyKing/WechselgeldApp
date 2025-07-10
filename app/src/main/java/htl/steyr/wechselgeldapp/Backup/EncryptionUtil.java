package htl.steyr.wechselgeldapp.Backup;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String encrypt(String input, String password) throws Exception {
        SecureRandom random = new SecureRandom();

        // Salt und IV generieren
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        random.nextBytes(salt);
        random.nextBytes(iv);

        // Schlüssel aus Passwort ableiten
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Verschlüsseln
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(input.getBytes("UTF-8"));

        // Salt + IV + verschlüsselte Daten kombinieren
        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    public static String decrypt(String input, String password) throws Exception {
        byte[] combined = Base64.decode(input, Base64.NO_WRAP);

        // Salt, IV und verschlüsselte Daten extrahieren
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 32];

        System.arraycopy(combined, 0, salt, 0, salt.length);
        System.arraycopy(combined, salt.length, iv, 0, iv.length);
        System.arraycopy(combined, salt.length + iv.length, encrypted, 0, encrypted.length);

        // Schlüssel aus Passwort ableiten
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Entschlüsseln
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, "UTF-8");
    }
}