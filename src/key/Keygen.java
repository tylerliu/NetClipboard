package key;

import ui.UserInterfacing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Key generation for AES-128/AES-256 suite
 */
public class Keygen {

    private static final int KEY_LEN = 1 << 5;

    static byte[] generateKey(int keyLen, byte[] seed) {
        assert seed.length >= keyLen;
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            UserInterfacing.printError(e);
        }

        byte[] out = new byte[keyLen];
        int written = 0;
        while (written < keyLen) {
            byte[] temp = digest.digest(Arrays.copyOfRange(seed, written, written + 32));
            System.arraycopy(temp, 0, out, written, Math.min(temp.length, out.length - written));
            written += temp.length;
        }
        return out;
    }

    public static byte[] generateKeyCMD() {
        return generateKeyCMD(KEY_LEN);
    }

    public static byte[] generateKeyCMD(int keyLen) {
        System.out.println("Enter Key generation seed, at least " + keyLen + " Characters long");
        byte[] initial = new byte[keyLen];
        try {
            System.in.readNBytes(initial, 0, initial.length);
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
        return generateKey(keyLen, initial);
    }

    public static void keyToFile(File file) {
        keyToFile(file, KEY_LEN);
    }

    public static void keyToFile(File file, int keyLen) {
        keyToFile(file, generateKeyCMD(keyLen));
    }

    public static void keyToFile(File file, byte[] key) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(Base64.getEncoder().encode(key));
            outputStream.close();
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
    }

    public static byte[] fileToKey(File file) {
        try {
            FileInputStream outputStream = new FileInputStream(file);
            byte[] bytes = outputStream.readAllBytes();
            outputStream.close();
            return Base64.getDecoder().decode(bytes);
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
        return null;
    }
}
