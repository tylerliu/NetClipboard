import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by TylerLiu on 2018/03/20.
 */
public class MFTest {
    public static class ArrayChannel implements ReadableByteChannel{


        ByteBuffer buffer = ByteBuffer.allocate(0x10000);
        @Override
        public int read(ByteBuffer dst) throws IOException {
            byte[] arr = new byte[dst.remaining()];
            buffer.get(arr);
            dst.put(arr);
            return arr.length;
        }

        public void putBuffer(Queue<ByteBuffer> buf){
            for (ByteBuffer b:buf) {
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

    public static void main(String[] args){
        try {
            ArrayChannel channel = new ArrayChannel();
            MultipleFormatOutBuffer OutBuffer = new MultipleFormatOutBuffer();
            OutBuffer.writeString("你好\n");

            channel.putBuffer(OutBuffer.getOutput());

            MultipleFormatInBuffer inBuffer = new MultipleFormatInBuffer(channel);
            System.out.println(inBuffer.getString());

        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
