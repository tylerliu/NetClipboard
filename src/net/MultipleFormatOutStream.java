package net;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
class MultipleFormatOutStream extends FilterOutputStream {

    /**
     * The internal buffer where data is stored.
     */
    private ByteBuffer buf;
    private byte type;

    public MultipleFormatOutStream(OutputStream outputStream) {
        super(outputStream);
        buf = ByteBuffer.allocate(0x10000);
    }

    /**
     * Flush the internal buffer
     */
    private void flushBuffer() throws IOException{
        int count = buf.position();
        byte[] head = new byte[]{type, (byte) (count >> 16), (byte) ((count >> 8) & 0XFF), (byte) (count & 0XFF)};
        super.write(head, 0, head.length);
        buf.flip();
        byte[] array = new byte[count];
        buf.get(array);
        super.write(array);
        buf.clear();
        type = 0;
    }


    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this buffer.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void writePayload(byte b[], int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            flushBuffer();
        }

        if (len > buf.remaining()) {
            int rem = buf.remaining();
            buf.put(b, off, buf.remaining());
            off += rem;
            len -= rem;
            flushBuffer();
            super.write(b, off, len);
        } else {
            buf.put(b, off, len);
        }
    }

    public synchronized void writePayload(byte b[]) throws IOException {
        writePayload(b, 0, b.length);
    }

    //format specific operations

    public synchronized void writeString(String s) throws IOException {
        assert buf.position() == 0;
        type = 1;
        writePayload(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeHTML(String s) throws IOException {
        assert buf.position() == 0;
        type = 3;
        writePayload(s.getBytes());
        flushBuffer();
    }

    public synchronized void writeEND() throws IOException {
        assert buf.position() == 0;
        type = 4;
        flushBuffer();
    }

    public synchronized void writeFiles() throws IOException {
        assert buf.position() == 0;
        type = 2;
        flushBuffer();
    }

}

