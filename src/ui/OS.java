package ui;

public class OS {

    private static boolean isEnvSet;
    private static boolean isDarwin;

    /**
     * @return true if this computer is a mac
     */
    public static boolean isMac() {
        if (isEnvSet) return isDarwin;
        String OS = System.getProperty("os.name", "generic").toLowerCase();
        isDarwin = (OS.contains("mac")) || (OS.contains("darwin"));
        isEnvSet = true;
        return isDarwin;
    }
}
