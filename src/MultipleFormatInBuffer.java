import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by TylerLiu on 2017/10/01.
 */
public class MultipleFormatInBuffer {

    public ByteBuffer buf;
    public byte type;
    public int length;
    public byte cont;
    ReadableByteChannel inChannel;


    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    public MultipleFormatInBuffer(ReadableByteChannel in) {
        inChannel = in;
    }

    private void loadNext() throws IOException {
        ByteBuffer head = ByteBuffer.allocate(4);
        inChannel.read(head);
        type = head.get(0);
        cont = head.get(1);
        length = Byte.toUnsignedInt(head.get(2));

        length += Byte.toUnsignedInt(head.get(3)) << 8;
        buf = ByteBuffer.allocate(length);
        inChannel.read(buf);
    }


    /**
     * Used only when sure the next is string
     */
    protected String getString() {
        byte ptype = type;
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type != 1 && (type != 0 || ptype != 1)) return null;
        return new String(buf.array()) + (cont != 0 ? getString() : "");
    }

    private String tryString() {
        if (type != 1) return null;
        return new String(buf.array()) + (cont != 0 ? getString() : "");
    }

    /**
     * Used only when sure the next is HTML
     */
    protected String getHTML() {
        byte ptype = type;
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type != 3 && (type != 0 || ptype != 3)) return null;
        return new String(buf.array()) + (cont != 0 ? getHTML() : "");
    }

    private String tryHTML() {
        if (type != 3) return null;
        return new String(buf.array()) + (cont != 0 ? getHTML() : "");
    }

    /**
     * Used only when sure the next is file
     */
    /*
    protected void readFile(OutputStream out, boolean close){
        byte ptype = type;
        try {
            loadNext();
            if (type != 2 && (type != 0 || ptype != 2)) return;
            out.write(buf, 0, cont == 1 ? buf.length : length);
            if (cont == 1) readFile(out, close);
            else if (close) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryFile(OutputStream out, boolean close){
        try {
            if (type != 2) return;
            out.write(buf, 0, cont == 1 ? buf.length : length);
            if (cont == 1) readFile(out, close);
            else if (close) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * @return [0] is the type, [1] is the data if applicable
     */
    protected Object[] readNext() {
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        switch (type) {
            case 0:
                return null;
            case 1:
                return new Object[]{1, tryString()};
            case 2:
                //tryFile(out, close);
                return new Object[]{2};
            case 3:
                return new Object[]{3, tryHTML()};
            case 4:
                return new Object[]{4};
            default:
                return null;
        }
    }
}
