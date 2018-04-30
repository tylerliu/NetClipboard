package tray;

public class Interfacing {

    private static boolean isCommandLine = false;

    public static void init() {
        if (!isCommandLine) ClipTray.init();
        LogWindow.getLogWindow();
    }

    /**
     * have to be called before init
     * @param isCommandLine1
     */
    public static void setIsCommandLine(boolean isCommandLine1) {
        isCommandLine = isCommandLine1;
    }

    public static void setConnStatus(String s) {
        if (isCommandLine) System.out.println(s);
        else ClipTray.setStatus(s);
    }

    public static void setClipStatus(String s) {
        if (isCommandLine) System.out.println(s);
        else ClipTray.setLastItem(s);
    }

    public static void printInfo(String s) {
        if (isCommandLine) System.out.println(s);
        else LogWindow.getLogWindow().showInfo(s + "\n");
    }

    public static void printError(Exception e) {
        if (isCommandLine) e.printStackTrace();
        else LogWindow.getLogWindow().showError(e);
    }
}
