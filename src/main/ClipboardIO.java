package main;

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
    public static boolean checkNew() {
        ContentType type = getSysClipboardFlavor();
        if (type == null) return false;
        switch (type) {
            case FILES:
                List<File> files = getSysClipboardFiles();
                if (isNewFiles(files)) {
                    lastType = ContentType.FILES;
                    lastFiles = files;
                    isFromRemote = false;
                    System.out.println("Local Clipboard New: " + files);
                    return true;
                }
                break;
            case STRING:
                String n = getSysClipboardText();
                if (isNewString(n)) {//have new
                    lastType = ContentType.STRING;
                    lastString = n;
                    isFromRemote = false;
                    System.out.println("Local Clipboard New: " + n);
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
    public static ContentType getSysClipboardFlavor() {
        try {
            //if (sysClip.isDataFlavorAvailable(DataFlavor.imageFlavor)) return ContentType.IMAGE;
            if (sysClip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) return ContentType.FILES;
            if (sysClip.isDataFlavorAvailable(DataFlavor.stringFlavor)) return ContentType.STRING;
        } catch (IllegalStateException e){
            if (!e.getMessage().contains("cannot open system clipboard")) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Get Text from Clipboard
     */
    public static String getSysClipboardText() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setSysClipboardText(String s) {
        lastType = ContentType.STRING;
        lastString = s;
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }

    public static List<File> getSysClipboardFiles() {
        try {
            return (List<File>) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void unsetSysClipboard(){
        lastString = "";
        isFromRemote = true;
        StringSelection ss = new StringSelection("");
        sysClip.setContents(ss, ss);
    }

    public enum ContentType {
        STRING, HTML, FILES, END
    }
}
