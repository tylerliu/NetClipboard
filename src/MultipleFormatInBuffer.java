import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

/**
 * Created by TylerLiu on 2017/10/01.
 */
class MultipleFormatInBuffer {

    private byte type;
    private int length;
    private byte cont;
    private ArrayDeque<ByteBuffer> input;
    private boolean isLastHead;

    public MultipleFormatInBuffer() {
        input = new ArrayDeque<>();
        input.add(ByteBuffer.allocate(4));
        isLastHead = true;
    }

    public ArrayDeque<ByteBuffer> getInput() {
        return input;
    }

    /**
     * create next ByteBuffer to fill
     */
    public void requestNext() {
        if (input.getLast().remaining() != 0) return;
        input.getLast().flip();
        if (isLastHead) {
            length = (input.getLast().get(1) << 16) + (Byte.toUnsignedInt(input.getLast().get(2)) << 8) + (Byte.toUnsignedInt(input.getLast().get(3)));
            input.add(ByteBuffer.allocate(length));
        } else {
            input.add(ByteBuffer.allocate(4));
        }
        isLastHead = !isLastHead;
    }

    /**
     * @return input is ready for reading
     */
    public boolean readyToRead() {
        if (!isLastHead || input.size() < 3) return false;
        ByteBuffer hl = input.pollLast();
        ByteBuffer il = input.pollLast();
        boolean ready = input.peekLast().get(1) == 0;
        input.add(il);
        input.add(hl);
        return ready;
    }

    private void loadNext() throws IOException {
        ByteBuffer head = input.poll();
        type = head.get(0);
        cont = head.get(1);
        length = (cont << 16) + (Byte.toUnsignedInt(head.get(2)) << 8) + (Byte.toUnsignedInt(head.get(3)));
    }


    /**
     * Used only when sure the next is string
     */
    String getString() {
        byte lastType = type;
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type != 1 && (type != 0 || lastType != 1)) return null;
        return new String(input.poll().array()) + (cont != 0 ? getString() : "");
    }

    private String tryString() {
        if (type != 1) return null;
        return new String(input.poll().array()) + (cont != 0 ? getString() : "");
    }

    /**
     * Used only when sure the next is HTML
     */
    private String getHTML() {
        byte lastType = type;
        try {
            loadNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (type != 3 && (type != 0 || lastType != 3)) return null;
        return new String(input.poll().array()) + (cont != 0 ? getHTML() : "");
    }

    private String tryHTML() {
        if (type != 3) return null;
        return new String(input.poll().array()) + (cont != 0 ? getHTML() : "");
    }

    /**
     * Used only when sure the next is file
     */
    /*
    protected void readFile(OutputStream out, boolean close){
        byte lastType = type;
        try {
            loadNext();
            if (type != 2 && (type != 0 || lastType != 2)) return;
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
    Object[] readNext() {
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
