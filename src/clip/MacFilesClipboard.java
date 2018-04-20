package clip;

import clip.c.macClipboardNative;

import java.io.File;
import java.io.IOException;
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

    /**
     * return the String of file used in AppleScript
     */
    public static String getFileRef(File file) {
        try {
            return "(POSIX file \"" + file.getCanonicalPath() + "\")";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAppleScript(List<File> files) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder.toString();
    }
}
