package clip;

import tray.Interfacing;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static ContentType lastType;
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
        ContentType type = getSysClipboardFlavor();
        if (type == null) return false;
        switch (type) {
            case FILES:
                List<File> files = getSysClipboardFiles();
                if (isNewFiles(files)) {
                    lastType = ContentType.FILES;
                    lastFiles = files;
                    isFromRemote = false;
                    Interfacing.printInfo("Local Clipboard New: " + files);
                    Interfacing.setClipStatus("Local Files");
                    return true;
                }
                break;
            case STRING:
                String n = getSysClipboardText();
                if (isNewString(n)) {//have new
                    lastType = ContentType.STRING;
                    lastString = n;
                    isFromRemote = false;
                    Interfacing.printInfo("Local Clipboard New: " + n);
                    if (n.contains("\n")) n = n.substring(0, n.indexOf('\n')) + "...";
                    Interfacing.setClipStatus("Local: " + (n.length() > 30 ? n.substring(0, 30) + "..." : n));
                    return true;
                }
                break;
            default:
        }
        return false;
    }

    private static boolean isNewString(String data) {
        return data != null && (ContentType.STRING != lastType || lastString == null || !lastString.equals(data));
    }

    private static boolean isNewFiles(List<File> data) {
        return data != null && (ContentType.FILES != lastType || lastFiles == null || !lastFiles.equals(data));
    }

    public static ContentType getLastType() {
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

    public static ContentType getContentType(int type) {
        return ContentType.values()[type - 1];
    }

    //TODO support Image?
    public synchronized static ContentType getSysClipboardFlavor() {
        try {
            //if (sysClip.isDataFlavorAvailable(DataFlavor.imageFlavor)) return ContentType.IMAGE;
            if (sysClip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) return ContentType.FILES;
            if (sysClip.isDataFlavorAvailable(DataFlavor.stringFlavor)) return ContentType.STRING;
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("cannot open system clipboard")) {
                Interfacing.printError(e);
            }
        }
        return null;
    }

    /**
     * Get Text from Clipboard
     */
    public static synchronized String getSysClipboardText() {
        try {
            return (String) sysClip.getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            Interfacing.printError(e);
            return null;
        }
    }

    public static synchronized void setSysClipboardText(String s) {
        lastType = ContentType.STRING;
        lastString = s;
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }

    public static synchronized List<File> getSysClipboardFiles() {
        try {
            return (List<File>) sysClip.getData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            Interfacing.printError(e);
            return null;
        }
    }

    public static synchronized void setSysClipboardFiles(List<File> files) {
        lastType = ContentType.FILES;
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

    public enum ContentType {
        STRING, HTML, FILES, END
    }
}
