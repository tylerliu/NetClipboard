package ui;

import javafx.embed.swing.JFXPanel;
import key.KeyUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.Objects;

public class UserInterfacing {

    private static File logFile;
    private static File keyFile;
    private static boolean isCommandLine = true;
    private static boolean isLog = false;
    private static PrintWriter writer;

    public static void init() {
        try {
            File currentDir = new File(UserInterfacing.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String currentDirPath = currentDir.getAbsolutePath();
            if (currentDirPath.endsWith(".jar"))
                currentDir = new File(currentDirPath.substring(0, currentDirPath.lastIndexOf(File.separatorChar)));
            logFile = new File(currentDir + File.separator + ".NetClipLog.txt");
            keyFile = new File(currentDir + File.separator + ".NetClipboardKey");
        } catch (URISyntaxException e) {
            System.exit(1);
        }
        new JFXPanel(); //initialize JavaFX Environment
        if (!isCommandLine) {
            ClipTray.init();
            LogWindow.init();
        }
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
     * set the interfacing mode of the program
     * have to be called before init
     *
     * @param isCommandLine is the current program running in command line mode
     */
    public static void setCommandLine(boolean isCommandLine) {
        UserInterfacing.isCommandLine = isCommandLine;
    }

    /**
     * set the logging mode of the program
     *
     * @param isLog is the current program logging the activity
     */
    public static void setLogging(boolean isLog) {
        UserInterfacing.isLog = isLog;
    }

    public static void setConnStatus(String s) {
        if (isLog) writer.println("<CONN>" + s);
        if (isCommandLine) System.out.println("<CONN>" + s);
        else ClipTray.setStatus(s);
    }

    public static void setClipStatus(String s) {
        if (isLog) writer.println("<CLIP>" + s);
        if (isCommandLine) System.out.println("<CLIP>" + s);
        else ClipTray.setLastItem(s);
    }

    public static void printInfo(String s) {
        if (isLog) writer.println(s);
        if (isCommandLine) System.out.println(s);
        else LogWindow.showInfo(s + "\n");
    }

    public static void printError(Exception e) {
        if (isLog) {
            e.printStackTrace(writer);
            writer.flush();
        }
        if (isCommandLine) e.printStackTrace();
        else LogWindow.showError(e);
    }

    public static void setKey(boolean isChange) {
        if (!isCommandLine) {
            if (isChange) {
                new Thread(() -> {
                    byte[] seed = KeyWindow.changeKey(true);
                    if (seed != null) KeyUtil.generateKeyFromSeed(seed);
                }).start();
            } else {
                byte[] seed = KeyWindow.changeKey(false);
                if (seed != null) KeyUtil.generateKeyFromSeed(seed);
            }
        } else {
            if (!isChange)
                System.out.println("Encryption key file not found. Please generate encryption key: ");
            KeyUtil.generateKey();
        }
    }

    public static File getSaveDir() {
        if (!isCommandLine) new JFXPanel();
        return DirChooser.chooseSaveDirectory();
    }

    public static void close() {
        if (isLog) writer.close();
    }

    public static InetAddress handleConnFail(String reason) {
        if (isLog) writer.println(reason);
        if (isCommandLine) {
            System.out.println(reason);
            return null;
        }
        return DirectConnectWindow.showConnFailWarning(reason);
    }

    public static File getKeyFile() {
        return Objects.requireNonNull(keyFile);
    }
}
