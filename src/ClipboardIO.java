import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static Object last;
    private static int lastHash;
    private static boolean isFromRemote;
    static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void checknew(){
        String n = getSysClipboardText();
        if (n.length() > 0 && lastHash != n.hashCode()){//have new
            lastHash = n.hashCode();
            isFromRemote = false;
            System.out.println("Local Clipboard New: " + n);
        }
    }

    public static int getLastHash() {
        return lastHash;
    }
    public static Object getLast() {
        return last;
    }
    public static boolean isLastFromRemote() {
        return isFromRemote;
    }


    /**
     * 从剪切板获得文字。
     */
    public static String getSysClipboardText() {
        Transferable clipTf = sysClip.getContents(null);
        if (clipTf != null && clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {

            try {
                if (clipTf.isDataFlavorSupported(DataFlavor.selectionHtmlFlavor)){
                    System.out.println(clipTf.getTransferData(DataFlavor.selectionHtmlFlavor).getClass());
                }
                return (String) clipTf.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static void setSysClipboardText(String s) {
        lastHash = s.hashCode();
        isFromRemote = true;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }
}
