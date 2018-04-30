package tray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class UserInterfacing {

    private static boolean isCommandLine = false;
    private static boolean isLog = false;
    private static File logFile = new File("./NetClipLog.txt");
    private static PrintWriter writer;
    public static void init() {
        if (!isCommandLine) ClipTray.init();
        LogWindow.init();
        try {
            if (isLog) writer = new PrintWriter(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isCommandLine() {
        return isCommandLine;
    }

    /**
     * have to be called before init
     * @param isCommandLine1
     */
    public static void setCommandLine(boolean isCommandLine1) {
        isCommandLine = isCommandLine1;
    }
    public static void setLogging(boolean isLog1) {
        isLog = isLog1;
    }

    public static void setConnStatus(String s) {
        if (isLog) writer.println(s);
        if (isCommandLine) System.out.println(s);
        else ClipTray.setStatus(s);
    }

    public static void setClipStatus(String s) {
        if (isLog) writer.println(s);
        if (isCommandLine) System.out.println(s);
        else ClipTray.setLastItem(s);
    }

    public static void printInfo(String s) {
        if (isLog) writer.println(s);
        if (isCommandLine) System.out.println(s);
        else LogWindow.showInfo(s + "\n");
    }

    public static void printError(Exception e) {
        if (isLog) e.printStackTrace(writer);
        if (isCommandLine) e.printStackTrace();
        else LogWindow.showError(e);
    }
}
