import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Created by TylerLiu on 2017/09/17.
 */
public class ClipboardListener extends Thread implements ClipboardOwner {
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();


    @Override
    public void run() {
        Transferable trans = sysClip.getContents(this);
        TakeOwnership(trans);

    }

    @Override
    public void lostOwnership(Clipboard c, Transferable t) {

        try {
            ClipboardListener.sleep(250);  //waiting e.g for loading huge elements like word's etc.
        } catch(Exception e) {
            System.out.println("Exception: " + e);
        }
        Transferable contents = sysClip.getContents(this);
        try {
            process_clipboard(contents, c);
        } catch (Exception ex) {
            System.out.println(ClipboardListener.class.getName() + ": "+ ex);
        }
        TakeOwnership(contents);


    }

    void TakeOwnership(Transferable t) {
        sysClip.setContents(t, this);
    }

    public void process_clipboard(Transferable t, Clipboard c) { //your implementation
        String tempText;
        Transferable trans = t;

        try {
            if (trans != null && trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                tempText = (String) trans.getTransferData(DataFlavor.stringFlavor);
                System.out.println(tempText);
            }

        } catch (Exception e) {
        }
    }

}
