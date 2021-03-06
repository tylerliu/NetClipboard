package format;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.FileTransferMode;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
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
        writePayload(TransferFormat.END_SIGNAL, new byte[]{});
    }

    public synchronized void writeModeSet(FileTransferMode.Mode mode) throws IOException {
        writePayload(TransferFormat.MODE_SET, new byte[]{(byte) mode.ordinal()});
    }


    //format specific operations

    public synchronized void writeFormatCount(byte count) throws IOException {
        writePayload(TransferFormat.FORMAT_COUNT, new byte[]{count});
    }

    public synchronized void writeFiles(int port, byte[] key) throws IOException {
        writePayload(TransferFormat.FILES, ByteBuffer.allocate(2 + key.length).putShort((short) port).put(key).array());
    }

    public synchronized void writeImageAsUrl() throws IOException {
        writePayload(TransferFormat.IMAGE, new byte[]{0});
    }

    public synchronized void writeImage(Image image) throws IOException {
        if (image.getUrl() != null) {
            writePayload(TransferFormat.IMAGE, ByteBuffer.allocate(1 + image.getUrl().length()).put((byte) 1).put(image.getUrl().getBytes()).array());
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(2);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", byteArrayOutputStream);
        byteArrayOutputStream.close();
        writePayload(TransferFormat.IMAGE, byteArrayOutputStream.toByteArray());
    }

    public synchronized void writeGeneralString(javafx.scene.input.DataFormat format, String str) throws IOException {
        assert format.getIdentifiers().size() == 1;
        String identifier = format.getIdentifiers().iterator().next();
        ByteBuffer buffer = ByteBuffer.allocate(identifier.getBytes().length + 1 + str.getBytes().length);
        buffer.put(identifier.getBytes()).put((byte) 0).put(str.getBytes());
        writePayload(TransferFormat.GENERAL_STRING, buffer.array());
    }

    public synchronized void writeByteBuffer(javafx.scene.input.DataFormat format, ByteBuffer value) throws IOException {
        assert format.getIdentifiers().size() == 1;
        String identifier = format.getIdentifiers().iterator().next();
        ByteBuffer buffer = ByteBuffer.allocate(identifier.getBytes().length + 1 + value.array().length);
        buffer.put(identifier.getBytes()).put((byte) 0).put(value.array());
        writePayload(TransferFormat.BYTE_BUFFER, buffer.array());
    }
}

