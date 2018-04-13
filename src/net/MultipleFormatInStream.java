package net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by TylerLiu on 2017/10/01.
 */
class MultipleFormatInStream extends FilterInputStream {

    private byte type;
    private int length;
    private byte cont;
    private byte[] payload;

    public MultipleFormatInStream(InputStream inputStream) {
        super(inputStream);
    }

    private void loadNext() throws IOException {
        byte[] head = new byte[4];
        super.readNBytes(head, 0, head.length);
        type = head[0];
        cont = head[1];
        length = (cont << 16) + (Byte.toUnsignedInt(head[2]) << 8) + (Byte.toUnsignedInt(head[3]));
        payload = new byte[length];
        try {
            super.readNBytes(payload, 0, payload.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Used only when sure the next is string
     */
    String getString() throws IOException {
        byte lastType = type;
        loadNext();
        if (type != 1 && (type != 0 || lastType != 1)) return null;
        return new String(payload) + (cont != 0 ? getString() : "");
    }

    private String tryString() throws IOException {
        if (type != 1) return null;
        return new String(payload) + (cont != 0 ? getString() : "");
    }

    /**
     * get file transfer info
     */
    private ByteBuffer tryFiles() throws IOException {
        if (type != 2) return null;
        return ByteBuffer.wrap(payload);
    }

    /**
     * Used only when sure the next is HTML
     */
    private String getHTML() throws IOException {
        byte lastType = type;
        loadNext();
        if (type != 3 && (type != 0 || lastType != 3)) return null;
        return new String(payload) + (cont != 0 ? getHTML() : "");
    }

    private String tryHTML() throws IOException {
        if (type != 3) return null;
        return new String(payload) + (cont != 0 ? getHTML() : "");
    }

    /**
     * @return [0] is the type, [1] is the data if applicable
     */
    Object[] readNext() throws IOException {
        loadNext();
        switch (type) {
            case 0:
                return null;
            case 1:
                return new Object[]{1, tryString()};
            case 2:
                return new Object[]{2, tryFiles()};
            case 3:
                return new Object[]{3, tryHTML()};
            case 4:
                return new Object[]{4};
            default:
                return null;
        }
    }
}
