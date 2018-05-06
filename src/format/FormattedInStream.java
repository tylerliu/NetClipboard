package format;

import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.util.Pair;
import net.FileTransferMode;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
        return FileTransferMode.Mode.values()[loadContent(TransferFormat.MODE_SET)[0]];
    }

    public byte getFormatCount() throws IOException {
        return loadContent(TransferFormat.FORMAT_COUNT)[0];
    }

    /**
     * get file transfer info
     */
    public ByteBuffer getFiles() throws IOException {
        return ByteBuffer.wrap(loadContent(TransferFormat.FILES));
    }

    public Image getImage(String potentialURL) throws IOException {
        byte[] content = loadContent(TransferFormat.IMAGE);
        if (content[0] == 0) return new Image(potentialURL, true);
        if (content[0] == 1) return new Image(new String(Arrays.copyOfRange(content, 1, content.length)), true);
        if (content[0] == 2) return new Image(new ByteArrayInputStream(Arrays.copyOfRange(content, 1, content.length)));
        return null;
    }

    public Pair<DataFormat, String> getGeneralString() throws IOException {
        byte[] bytes = loadContent(TransferFormat.GENERAL_STRING);
        int index = new String(bytes).indexOf('\0');
        String identifier = new String(Arrays.copyOfRange(bytes, 0, index));
        String data = new String(Arrays.copyOfRange(bytes, index + 1, bytes.length));
        DataFormat format = DataFormat.lookupMimeType(identifier);
        if (format == null) format = new DataFormat(identifier);
        return new Pair<>(format, data);
    }

    public Pair<DataFormat, ByteBuffer> getByteBuffer() throws IOException {
        byte[] bytes = loadContent(TransferFormat.BYTE_BUFFER);
        int index = new String(bytes).indexOf('\0');
        String identifier = new String(Arrays.copyOfRange(bytes, 0, index));
        ByteBuffer data = ByteBuffer.wrap(Arrays.copyOfRange(bytes, index + 1, bytes.length));
        DataFormat format = DataFormat.lookupMimeType(identifier);
        if (format == null) format = new DataFormat(identifier);
        return new Pair<>(format, data);
    }
}
