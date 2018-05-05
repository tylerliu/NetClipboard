package format;

import net.FileTransferMode;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * an output stream wrapper
 */
public class FormattedOutStream extends FilterOutputStream {

    public FormattedOutStream(OutputStream outputStream) {
        super(outputStream);
    }

    private synchronized void writePayload(byte type, byte b[]) throws IOException {
        super.write(type);
        super.write(ByteBuffer.allocate(8).putLong(b.length).array());
        write(b, 0, b.length);
    }

    public synchronized void writeEND() throws IOException {
        writePayload(DataFormat.END_SIGNAL, new byte[]{});
    }

    public synchronized void writeModeSet(FileTransferMode.Mode mode) throws IOException {
        writePayload(DataFormat.MODE_SET, new byte[]{(byte) mode.ordinal()});
    }



    //format specific operations

    public synchronized void writeFormatCount(byte count) throws IOException {
        writePayload(DataFormat.FORMAT_COUNT, new byte[]{count});
    }

    public synchronized void writeSTRING(String s) throws IOException {
        writePayload(DataFormat.STRING, s.getBytes());
    }

    public synchronized void writeSTRING(byte format, String s) throws IOException {
        writePayload(format, s.getBytes());
    }

    public synchronized void writeFiles(int port, byte[] key) throws IOException {
        writePayload(DataFormat.FILES, ByteBuffer.allocate(2 + key.length).putShort((short) port).put(key).array());
    }
}

