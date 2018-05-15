package key;

import ui.UserInterfacing;

import java.io.File;

public class KeyUtil {

    public static final int KEY_LEN = 32;

    private static File getKeyFile() {
        return UserInterfacing.getKeyFile();
    }

    public static byte[] getKey() {
        return key.Keygen.fileToKey(getKeyFile());
    }

    public static void generateKey() {
        Keygen.keyToFile(getKeyFile());
    }

    public static void generateKeyFromSeed(byte[] seed) {
        Keygen.keyToFile(getKeyFile(), Keygen.generateKey(KEY_LEN, seed));
    }

    public static boolean isKeyExist() {
        return getKeyFile().exists();
    }
}
