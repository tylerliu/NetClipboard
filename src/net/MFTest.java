package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;

/**
 * Created by TylerLiu on 2018/03/20.
 */
/*
public class MFTest {
    public static void main(String[] args) {
        try {
            ArrayChannel channel = new ArrayChannel();
            MultipleFormatOutStream OutBuffer = new MultipleFormatOutStream();
            OutBuffer.writeString("你好\n");

            channel.putBuffer(OutBuffer.getOutput());

            MultipleFormatInBuffer inBuffer = new MultipleFormatInBuffer();
            channel.read(inBuffer.getInput().peekLast());
            while (!inBuffer.getInput().peekLast().hasRemaining()) {
                inBuffer.requestNext();
                channel.read(inBuffer.getInput().peekLast());
            }

            System.out.println(inBuffer.readyToRead() + " " + inBuffer.getString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static class ArrayChannel implements ReadableByteChannel {


        ByteBuffer buffer = ByteBuffer.allocate(0x10000);

        @Override
        public int read(ByteBuffer dst) throws IOException {
            byte[] arr = new byte[Math.min(dst.remaining(), buffer.remaining())];
            buffer.get(arr);
            dst.put(arr);
            return arr.length;
        }

        public void putBuffer(Queue<ByteBuffer> buf) {
            for (ByteBuffer b : buf) {
                buffer.put(b.array());
            }
            buffer.flip();
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {

        }
    }
}
*/
