import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    public enum ContentType{STRING, HTML, FILES};
    public static ContentType lastType;
    private static Object last;
    private static boolean isFromRemote;
    static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * check for new things in the clipboard
     * @return true if change happens
     */
    public static boolean checknew(){
        String n = getSysClipboardHTML();
        if (n.length() > 0 && last != null && !last.equals(n)){//have new
            lastType = ContentType.HTML;
            last = n;
            isFromRemote = false;
            System.out.println("Local Clipboard New HTML: " + n);
            return true;
        }
        n = getSysClipboardText();
        if (n.length() > 0 && last != null && !last.equals(n)){//have new
            lastType = ContentType.STRING;
            last = n;
            isFromRemote = false;
            System.out.println("Local Clipboard New: " + n);
            return true;
        }
        return false;
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

    public static ContentType getContentType(int type){
        switch (type) {
            case 1:
                return ContentType.STRING;
            case 2:
                return ContentType.FILES;
            case 3:
                return ContentType.HTML;
            default:
                return null;
        }
    }

    /**
     * 从剪切板获得文字。
     */
    public static String getSysClipboardText() {
        Transferable clipTf = sysClip.getContents(null);
        if (clipTf != null && clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) clipTf.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void setSysClipboardText(String s) {
        lastType = ContentType.STRING;
        last = s;
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }

    /**
     * 从剪切板获得文字。
     */
    public static String getSysClipboardHTML() {
        Transferable clipTf = sysClip.getContents(null);
        if (clipTf != null && clipTf.isDataFlavorSupported(DataFlavor.selectionHtmlFlavor)) {
            try {
                return (String) clipTf.getTransferData(DataFlavor.selectionHtmlFlavor) + '\0' + clipTf.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void setSysClipboardHTML(String s) {
        lastType = ContentType.HTML;
        last = s;
        isFromRemote = true;
        HTMLSelection ss = new HTMLSelection(s);
        sysClip.setContents(ss, ss);
    }
}
