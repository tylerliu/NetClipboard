package tray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Interfacing {

    private static boolean isCommandLine = false;
    private static boolean isLog = false;
    private static File logFile = new File("./NetClipLog.txt");
    private static PrintWriter writer;
    public static void init() {
        if (!isCommandLine) ClipTray.init();
        LogWindow.getLogWindow();
        try {
            if (isLog) writer = new PrintWriter(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        if (isLog) writer.println(s);
        else ClipTray.setStatus(s);
    }

    public static void setClipStatus(String s) {
        if (isCommandLine) System.out.println(s);
        if (isLog) writer.println(s);
        else ClipTray.setLastItem(s);
    }

    public static void printInfo(String s) {
        if (isCommandLine) System.out.println(s);
        if (isLog) writer.println(s);
        else LogWindow.getLogWindow().showInfo(s + "\n");
    }

    public static void printError(Exception e) {
        if (isCommandLine) e.printStackTrace();
        if (isLog) e.printStackTrace(writer);
        else LogWindow.getLogWindow().showError(e);
    }
}
