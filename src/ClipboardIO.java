import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.util.ArrayDeque;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {
    static ArrayDeque<String> queue = new ArrayDeque<>();
    static Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    public static void checknew(){
        String n = getSysClipboardText();
        if (n.length() > 0 && !n.equals(queue.getLast())){//have new
            queue.add(getSysClipboardText());
            System.out.println(queue.getLast());
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
}
