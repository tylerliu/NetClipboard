package net;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

/**
 * Created by TylerLiu on 2017/10/01.
 */
class MultipleFormatInBuffer {

    private byte type;
    private int length;
    private byte cont;
    private ArrayDeque<byte[]> input;
    private boolean isLastHead;

    public MultipleFormatInBuffer() {
        input = new ArrayDeque<>();
        input.add(new byte[4]);
        isLastHead = true;
    }

    public ArrayDeque<byte[]> getInput() {
        return input;
    }

    /**
     * create next ByteBuffer to fill
     */
    public void requestNext() {
        if (isLastHead) {
            length = (input.getLast()[1] << 16) + (Byte.toUnsignedInt(input.getLast()[2]) << 8) + (Byte.toUnsignedInt(input.getLast()[3]));
            input.add(new byte[length]);
        } else {
            input.add(new byte[4]);
        }
        isLastHead = !isLastHead;
    }

    /**
     * @return input is ready for reading
     */
    public boolean readyToRead() {
        if (!isLastHead || input.size() < 3) return false;
        byte[] hl = input.pollLast();
        byte[] il = input.pollLast();
        boolean ready = input.peekLast()[1] == 0;
        input.add(il);
        input.add(hl);
        return ready;
    }

    private void loadNext() {
        byte[] head = input.poll();
        type = head[0];
        cont = head[1];
        length = (cont << 16) + (Byte.toUnsignedInt(head[2]) << 8) + (Byte.toUnsignedInt(head[3]));
    }


    /**
     * Used only when sure the next is string
     */
    String getString() {
        byte lastType = type;
        loadNext();
        if (type != 1 && (type != 0 || lastType != 1)) return null;
        return new String(input.poll()) + (cont != 0 ? getString() : "");
    }

    private String tryString() {
        if (type != 1) return null;
        return new String(input.poll()) + (cont != 0 ? getString() : "");
    }

    /**
     * Used only when sure the next is HTML
     */
    private String getHTML() {
        byte lastType = type;
        loadNext();
        if (type != 3 && (type != 0 || lastType != 3)) return null;
        return new String(input.poll()) + (cont != 0 ? getHTML() : "");
    }

    private String tryHTML() {
        if (type != 3) return null;
        return new String(input.poll()) + (cont != 0 ? getHTML() : "");
    }

    /**
     * @return [0] is the type, [1] is the data if applicable
     */
    Object[] readNext() {
        loadNext();
        switch (type) {
            case 0:
                return null;
            case 1:
                return new Object[]{1, tryString()};
            case 2:
                input.poll();
                return new Object[]{2};
            case 3:
                return new Object[]{3, tryHTML()};
            case 4:
                input.poll();
                return new Object[]{4};
            default:
                return null;
        }
    }
}
