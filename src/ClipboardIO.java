import java.util.List;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static ContentType lastType;
    private static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static Object last;
    private static boolean isFromRemote;

    /**
     * check for new things in the clipboard
     *
     * @return true if change happens
     */
    public static boolean checkNew() {

        String n = getSysClipboardText();
        if (isNew(ContentType.STRING, n)) {//have new
            lastType = ContentType.STRING;
            last = n;
            isFromRemote = false;
            System.out.println("Local Clipboard New: " + n);
            return true;
        }
        if (isSame(ContentType.STRING, n)) return false;
        return false;
    }

    private static boolean isNew(ContentType type, Object data) {
        if (data == null) return false;
        if (type == ContentType.STRING && ((String) data).length() == 0) return false;
        if (type == ContentType.HTML && ((String) data).length() <= 1) return false;

        return type != lastType || last == null || !last.equals(data);
    }

    private static boolean isSame(ContentType type, Object data) {
        return type == lastType && data != null && last != null && last.equals(data);
    }

    public static ContentType getLastType() {
        return lastType;
    }

    public static Object getLast() {
        return last;
    }

    public static boolean isLastFromRemote() {
        return isFromRemote;
    }

    public static ContentType getContentType(int type) {
        return ContentType.values()[type - 1];
    }

    //TODO support Image?
    public static ContentType getSysClipboardFlavor() {
        //if (sysClip.isDataFlavorAvailable(DataFlavor.imageFlavor)) return ContentType.IMAGE;
        if (sysClip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) return ContentType.FILES;
        if (sysClip.isDataFlavorAvailable(DataFlavor.stringFlavor)) return ContentType.STRING;
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
        last = s;
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }

    public static List<File> getSysClipboardFiles(){
        try {
            return (List<File>) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setSysCLipboardFiles(List<File> files) {
        lastType = ContentType.FILES;
        last = files;
        isFromRemote = true;
        FileTransfer.FileTransferable fileTransferable = new FileTransfer.FileTransferable(files);
        sysClip.setContents(fileTransferable, fileTransferable);
    }

    public enum ContentType {
        STRING, HTML, FILES, END
    }
}
