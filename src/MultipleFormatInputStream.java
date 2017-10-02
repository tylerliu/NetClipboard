import java.io.*;

/**
 * Created by TylerLiu on 2017/10/01.
 */
public class MultipleFormatInputStream extends FilterInputStream {

    public byte[] buf;
    public byte type;
    public int length;
    public byte cont;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    public MultipleFormatInputStream(InputStream in) {
        super(in);
        buf = new byte[0x10000];
    }

    private void loadNext() throws IOException {
        type = (byte) read();
        cont = (byte) read();
        length = read();
        length += read() << 8;
        read(buf, 0, length);
    }


    /**
     * Used only when sure the next is string
     */
    protected String getString(){
        byte ptype = type;
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type != 1 && (type != 0 || ptype != 1)) return null;
        if (cont == 0) return new String(buf, 0, length);
        else return new String(buf, 0, buf.length) + getString();
    }

    private String tryString(){
        if (type != 1) return null;
        if (cont == 0) return new String(buf, 0, length);
        else return new String(buf, 0, buf.length) + getString();
    }

    /**
     * Used only when sure the next is file
     */
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
    }

    /**
     *
     * @param out OutputStream if the object is file
     * @param close OutputStream if the object is file
     * @return [0] is the type, [1] is the data if applicable
     */
    protected Object[] readNext(OutputStream out, boolean close) {
        try {
            loadNext();
        } catch (EOFException e){
        System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        switch(type){
            case 0:
                return null;
            case 1:
                return new Object[]{1, tryString()};
            case 2:
                tryFile(out, close);
                return new Object[]{2};
            default:
                return null;
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        buf = null;
    }
}
