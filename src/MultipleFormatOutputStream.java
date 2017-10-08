import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * a class output stream that can transfer multiple format of file
 *
 * headers:
 * byte 0: kind of format:
 * 0: continue
 * 1: String/SegmentedHTML
 * 2: ZipFile
 * 3: HTML
 *
 * byte 1: indicate the number it is length is exceeding 0XFFFF
 * if yes, it will be joined with next one
 * if exactly 0x10000, next one have to be length 0
 *
 * byte 2, 3: length of file
 *
 * Data
 *
 * Created by TylerLiu on 2017/10/01.
 */
public class MultipleFormatOutputStream extends FilterOutputStream {

    /**
     * The internal buffer where data is stored.
     */
    protected byte buf[];
    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;
    /**
     * The format type of the buffer.
     */
    protected byte type;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param out the underlying output stream to be assigned to
     *            the field <tt>this.out</tt> for later use, or
     *            <code>null</code> if this instance is to be
     *            created without an underlying stream.
     */
    public MultipleFormatOutputStream(OutputStream out) {
        super(out);
        buf = new byte[0x10000];
    }
    /** Flush the internal buffer */
    private void flushBuffer() throws IOException {
        out.write(new byte[]{type, (byte)(count >> 16), (byte)(count & 0XFF), (byte)((count >> 8) & 0XFF)});
        out.write(buf, 0, count);
        count = 0;
        type = 0;
    }


    private void copyStream(InputStream input) throws IOException {
        assert count == 0;
        while ((count = input.read(buf)) != -1)
        {
            if (count < buf.length) {
                flushBuffer();
                return;
            }
            flushBuffer();
        }
        count = 0;
        flushBuffer();
    }

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void write(int b) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }
        buf[count++] = (byte)b;
    }



    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this buffered output stream.
     *
     * <p> Ordinarily this method stores bytes from the given array into this
     * stream's buffer, flushing the buffer to the underlying output stream as
     * needed.  If the requested length is at least as large as this stream's
     * buffer, however, then this method will flush the buffer and write the
     * bytes directly to the underlying output stream.  Thus redundant
     * <code>BufferedOutputStream</code>s will not copy data unnecessarily.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void write(byte b[], int off, int len) throws IOException {
        if (count >= buf.length) {
            flushBuffer();
        }

        if (len >= buf.length - count) {
            System.arraycopy(b, off, buf, count, buf.length - count);
            off += buf.length - count;
            len -= buf.length - count;
            count = buf.length;
            flushBuffer();
            write(b, off, len);
        } else {
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }
    }

    /**
     * Flushes this buffered output stream. This forces any buffered
     * output bytes to be written out to the underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        FilterOutputStream#out
     */
    public synchronized void flush() throws IOException {
        if (count != 0) flushBuffer();
        out.flush();
    }

    //format specific operations

    public synchronized void writeString(String s) throws IOException {
        assert count == 0;
        type = 1;
        write(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeHTML(String s) throws IOException {
        assert count == 0;
        type = 3;
        write(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeFile(InputStream in, boolean close) throws IOException{
        assert count == 0;
        type = 2;

        //read from input stream
        copyStream(in);

        if (close) in.close();
    }

    public synchronized void writeFileHead(){
        //TODO Finish this
        assert count == 0;
        type = 2;
    }

    public synchronized void writeFileEnd(){
        try {
            flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

