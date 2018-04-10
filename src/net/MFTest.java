package net;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;

/**
 * Created by TylerLiu on 2018/03/20.
 */
public class MFTest {
    public static void main(String[] args) {
        try {

            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);

            MultipleFormatOutStream outStream = new MultipleFormatOutStream(out);
            MultipleFormatInStream inStream = new MultipleFormatInStream(in);
            outStream.writeString("你好\n");

            System.out.println(inStream.readNext()[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
