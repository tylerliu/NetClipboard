import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by TylerLiu on 2018/03/20.
 */
public class MFTest {
    public static class ArrayChannel implements WritableByteChannel, ReadableByteChannel{

        ByteBuffer buffer = ByteBuffer.allocate(0x10000);
        @Override
        public int read(ByteBuffer dst) throws IOException {
            byte[] arr = new byte[dst.remaining()];
            buffer.get(arr);
            dst.put(arr);
            return arr.length;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            buffer.put(src);
            return 0;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {

        }

        public void toRead(){
            buffer.limit(buffer.position());
            buffer.rewind();
            byte[] arr = new byte[buffer.limit()];
            buffer.get(arr);
            System.out.println("size: " + buffer.position());
            for (byte b: arr){
                System.out.println(b + " " );
            }
            System.out.println();
            buffer.rewind();
        }
    }

    public static void main(String[] args){
        try {
            ArrayChannel channel = new ArrayChannel();
            MultipleFormatOutBuffer OutBuffer = new MultipleFormatOutBuffer(channel);
            OutBuffer.writeString("你好\n");

            channel.toRead();

            MultipleFormatInBuffer inBuffer = new MultipleFormatInBuffer(channel);
            System.out.println(inBuffer.getString());

        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
