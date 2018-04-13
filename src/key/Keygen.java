package key;

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

    public static byte[] generateKey() {
        return generateKey(KEY_LEN);
    }

    public static byte[] generateKey(int keyLen) {
        System.out.println("Enter Key generation seed, as complex as possible, at least " + (keyLen * 3 / 2) + " Characters long");
        byte[] initial = new byte[keyLen * 3 / 2];
        try {
            System.in.readNBytes(initial, 0, initial.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] out = new byte[keyLen];
        int written = 0;
        while (written < keyLen) {
            byte[] temp = digest.digest(Arrays.copyOfRange(initial, written, (written + 32 * 3 / 2)));
            for (int i = 0; i < Math.min(temp.length, out.length - written); i++) {
                out[written + i] = temp[i];
            }
            written += temp.length;
        }
        return out;
    }

    public static void keyToFile(File file) {
        keyToFile(file, KEY_LEN);
    }

    public static void keyToFile(File file, int keyLen) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(Base64.getEncoder().encode(generateKey(keyLen)));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte[] fileToKey(File file) {
        try {
            FileInputStream outputStream = new FileInputStream(file);
            byte[] bytes = outputStream.readAllBytes();
            outputStream.close();
            return Base64.getDecoder().decode(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
