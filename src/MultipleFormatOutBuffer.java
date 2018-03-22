import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * a class output stream that can transfer multiple format of file
 * <p>
 * headers:
 * byte 0: kind of format:
 * 0: continue
 * 1: String/SegmentedHTML
 * 2: ZipFile
 * 3: HTML
 * 4: END_SIGNAL
 * <p>
 * byte 1: indicate the number it is length is exceeding 0XFFFF
 * if yes, it will be joined with next one
 * if exactly 0x10000, next one have to be length 0
 * <p>
 * byte 2, 3: length of file
 * <p>
 * Data
 * <p>
 * Created by TylerLiu on 2017/10/01.
 */
class MultipleFormatOutBuffer {

    /**
     * The internal buffer where data is stored.
     */
    private ByteBuffer buf;
    private byte type;

    private ArrayDeque<ByteBuffer> output;

    public MultipleFormatOutBuffer() {
        buf = ByteBuffer.allocate(0x10000);
        output = new ArrayDeque<>();
    }

    public Queue<ByteBuffer> getOutput() {
        return output;
    }

    /**
     * Flush the internal buffer
     */
    private void flushBuffer() throws IOException {
        int count = buf.position();
        output.add(ByteBuffer.wrap(new byte[]{type, (byte) (count >> 16), (byte) ((count >> 8) & 0XFF), (byte) (count & 0XFF)}));
        buf.flip();
        byte[] store = new byte[count];
        buf.get(store);
        output.add(ByteBuffer.wrap(store));
        buf.clear();
        type = 0;
    }

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param b the byte to be written.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void write(int b) throws IOException {
        if (!buf.hasRemaining()) {
            flushBuffer();
        }
        buf.put((byte) b);
    }


    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this buffered output stream.
     * <p>
     * <p> Ordinarily this method stores bytes from the given array into this
     * stream's buffer, flushing the buffer to the underlying output stream as
     * needed.  If the requested length is at least as large as this stream's
     * buffer, however, then this method will flush the buffer and write the
     * bytes directly to the underlying output stream.  Thus redundant
     * <code>BufferedOutputStream</code>s will not copy data unnecessarily.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    private synchronized void write(byte b[], int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            flushBuffer();
        }

        if (len > buf.remaining()) {
            int rem = buf.remaining();
            buf.put(b, off, buf.remaining());
            off += rem;
            len -= rem;
            flushBuffer();
            write(b, off, len);
        } else {
            buf.put(b, off, len);
        }
    }

    private synchronized void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    //format specific operations

    public synchronized void writeString(String s) throws IOException {
        assert buf.position() == 0;
        type = 1;
        write(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeHTML(String s) throws IOException {
        assert buf.position() == 0;
        type = 3;
        write(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeEND() throws IOException {
        assert buf.position() == 0;
        type = 4;
        flushBuffer();
    }

    /*
    public synchronized void writeFile(InputStream in, boolean close) throws IOException{
        assert buf.position() == 0;
        type = 2;

        //read from input stream
        copyStream(in);

        if (close) in.close();
    }


    public synchronized void writeFileHead(){
        //TODO Finish this
        assert buf.position() == 0;
        type = 2;
    }

    public synchronized void writeFileEnd(){
        try {
            flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

}

