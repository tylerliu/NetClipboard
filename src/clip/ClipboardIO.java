package clip;

import ui.UserInterfacing;
import format.DataFormat;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static byte lastType;
    private static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static String lastString;
    private static List<File> lastFiles;
    private static boolean isFromRemote;

    /**
     * check for new things in the clipboard
     *
     * @return true if change happens
     */
    public static synchronized boolean checkNew() {
        byte type = getSysClipboardFlavor();
        if (type == 0) return false;
        switch (type) {
            case DataFormat.FILES:
                List<File> files = getSysClipboardFiles();
                if (isNewFiles(files)) {
                    lastType = DataFormat.FILES;
                    lastFiles = files;
                    isFromRemote = false;
                    UserInterfacing.printInfo("Local Clipboard New: " + files);
                    UserInterfacing.setClipStatus("Local Files");
                    return true;
                }
                break;
            case DataFormat.STRING:
                String n = getSysClipboardText();
                if (isNewString(n)) {//have new
                    lastType = DataFormat.STRING;
                    lastString = n;
                    isFromRemote = false;
                    UserInterfacing.printInfo("Local Clipboard New: " + n);
                    if (n.contains("\n")) n = n.substring(0, n.indexOf('\n')) + "...";
                    UserInterfacing.setClipStatus("Local: " + (n.length() > 30 ? n.substring(0, 30) + "..." : n));
                    return true;
                }
                break;
            default:
        }
        return false;
    }

    private static boolean isNewString(String data) {
        return data != null && (DataFormat.STRING != lastType || lastString == null || !lastString.equals(data));
    }

    private static boolean isNewFiles(List<File> data) {
        return data != null && (DataFormat.FILES != lastType || lastFiles == null || !lastFiles.equals(data));
    }

    public static byte getLastType() {
        return lastType;
    }

    public static String getLastString() {
        return lastString;
    }

    public static List<File> getLastFiles() {
        return lastFiles;
    }

    public static boolean isLastFromRemote() {
        return isFromRemote;
    }

    //TODO support Image?
    public synchronized static byte getSysClipboardFlavor() {
        try {
            //if (sysClip.isDataFlavorAvailable(DataFlavor.imageFlavor)) return DataFormat.IMAGE;
            if (sysClip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) return DataFormat.FILES;
            if (sysClip.isDataFlavorAvailable(DataFlavor.stringFlavor)) return DataFormat.STRING;
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("cannot open system clipboard")) {
                UserInterfacing.printError(e);
            }
        }
        return DataFormat.NULL;
    }

    /**
     * Get Text from Clipboard
     */
    public static synchronized String getSysClipboardText() {
        try {
            return (String) sysClip.getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            UserInterfacing.printError(e);
            return null;
        }
    }

    public static synchronized void setSysClipboardText(String s) {
        lastType = DataFormat.STRING;
        lastString = s;
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }

    public static synchronized List<File> getSysClipboardFiles() {
        try {
            return (List<File>) sysClip.getData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            UserInterfacing.printError(e);
            return null;
        }
    }

    public static synchronized void setSysClipboardFiles(List<File> files) {
        lastType = DataFormat.FILES;
        lastFiles = files;
        isFromRemote = true;
        if (MacFilesClipboard.isMac()) {
            MacFilesClipboard.setMacSysClipboardFile(files);
        } else {
            FilesTransferable fileTransferable = new FilesTransferable(files);
            sysClip.setContents(fileTransferable, fileTransferable);
        }
    }

    public static synchronized void unsetSysClipboard() {
        lastString = "";
        isFromRemote = true;
        StringSelection ss = new StringSelection("");
        sysClip.setContents(ss, ss);
    }
}
