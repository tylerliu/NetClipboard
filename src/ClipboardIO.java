import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static String last = "";
    static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void checknew(){
        String n = getSysClipboardText();
        if (n.length() > 0 && !last.equals(n)){//have new
            last = n;
            System.out.println("Local Clipboard New: " + last);
        }
    }

    public static String getLast() {
        return last;
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
        if (getSysClipboardText().equals(s))return;
        StringSelection ss = new StringSelection(s);
        sysClip.setContents(ss, ss);
    }
}
