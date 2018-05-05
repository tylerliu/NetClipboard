package format;

import net.FileTransferMode;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An input stream wrapper
 */
public class FormattedInStream extends FilterInputStream {

    private byte type;
    private long length;
    private boolean read;

    public FormattedInStream(InputStream inputStream) {
        super(inputStream);
    }

    public int nextEntry() throws IOException {
        if (!read) skip(length);
        byte[] head = new byte[9];
        super.readNBytes(head, 0, head.length);
        ByteBuffer buf = ByteBuffer.wrap(head);
        type = buf.get();
        length = buf.getLong();
        read = false;
        return type;
    }

    private byte[] loadContent(int type) throws IOException {
        if (this.type != type) throw new IOException("Packet Type Does Not Match");
        read = true;
        byte[] content = new byte[(int) length];
        readNBytes(content, 0, (int) length);
        return content;
    }


    public FileTransferMode.Mode getMode() throws IOException {
        return FileTransferMode.Mode.values()[loadContent(DataFormat.MODE_SET)[0]];
    }

    public byte getFormatCount() throws IOException {
        return loadContent(DataFormat.FORMAT_COUNT)[0];
    }


    /**
     * Used only when sure the next is string
     */
    public String getString() throws IOException {
        return new String(loadContent(DataFormat.STRING));
    }

    /**
     * get file transfer info
     */
    public ByteBuffer getFiles() throws IOException {
        return ByteBuffer.wrap(loadContent(DataFormat.FILES));
    }

    /**
     * Used only when sure the next is HTML
     */
    public String getString(byte format) throws IOException {
        return new String(loadContent(format));
    }
}
