import java.awt.datatransfer.*;
import java.io.*;


/**
 * A <code>Transferable</code> which implements the capability required
 * to transfer a <code>String</code>.
 *
 * This <code>Transferable</code> properly supports
 * <code>DataFlavor.stringFlavor</code>
 * and all equivalent flavors. Support for
 * <code>DataFlavor.plainTextFlavor</code>
 * and all equivalent flavors is <b>deprecated</b>. No other
 * <code>DataFlavor</code>s are supported.
 *
 * @see java.awt.datatransfer.DataFlavor#stringFlavor
 * @see java.awt.datatransfer.DataFlavor#plainTextFlavor
 */
public class HTMLSelection implements Transferable, ClipboardOwner {

    private static final int STRING = 0;
    private static final int PLAIN_TEXT = 4;

    private static final DataFlavor[] flavors = {
            DataFlavor.stringFlavor,
            DataFlavor.allHtmlFlavor,
            DataFlavor.fragmentHtmlFlavor,
            DataFlavor.selectionHtmlFlavor,
            DataFlavor.plainTextFlavor // deprecated
    };

    private String HTMLData;
    private String data;

    public HTMLSelection(String data) {
        this(data.substring(0, data.indexOf('\0')), data.substring(data.indexOf('\0') + 1));
    }

    public HTMLSelection(String HTMLData, String data){
        this.HTMLData = HTMLData;
        this.data = data;
    }

    public DataFlavor[] getTransferDataFlavors() {
        // returning flavors itself would allow client code to modify
        // our internal behavior
        return flavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
    {
        // JCK Test StringSelection0007: if 'flavor' is null, throw NPE
        if (flavor.equals(flavors[STRING])) {
            return data;
        } else if (flavor.equals(flavors[PLAIN_TEXT])) {
            return new StringReader(data == null ? "" : data);
        } else if (this.isDataFlavorSupported(flavor)){
            return HTMLData;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}