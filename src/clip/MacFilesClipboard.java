package clip;

import clip.c.macClipboardNative;

import java.io.File;
import java.util.List;

/**
 * a class to support mac clipboard pasting
 */
public class MacFilesClipboard {

    private static boolean isEnvSet;
    private static boolean isDarwin;
    /**
     *
     * @return
     */
    public static boolean isMac() {
        if (isEnvSet) return isDarwin;
        String OS = System.getProperty("os.name", "generic").toLowerCase();
        isDarwin = (OS.contains("mac")) || (OS.contains("darwin"));
        isEnvSet = true;
        return isDarwin;
    }

    public static void setMacSysClipboardFile(List<File> files) {
        if (isMac()) macClipboardNative.setClipboardFiles(files);
    }
}
