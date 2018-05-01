package key;

import java.io.File;

public class KeyUtil {

    private static final File keyFile = new File("./.NetClipboardKey");

    public static File getKeyFile() {
        return keyFile;
    }

    public static byte[] getKey() {
        return key.Keygen.fileToKey(keyFile);
    }

    public static void generateKey() {
        Keygen.keyToFile(keyFile);
    }

    public static void generateKeyFromSeed(byte[] seed) {
        Keygen.keyToFile(keyFile, Keygen.generateKey(32, seed));
    }

    public static boolean isKeyExist() {
        return keyFile.exists();
    }
}
