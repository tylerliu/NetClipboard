package format;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by TylerLiu on 2018/03/20.
 */
public class MFTest {
    public static void main(String[] args) {
        try {

            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);

            FormattedOutStream outStream = new FormattedOutStream(out);
            FormattedInStream inStream = new FormattedInStream(in);
            outStream.writeSTRING("你好\n");
            inStream.nextEntry();
            System.out.println(inStream.getString());
            outStream.writeFiles(8800, new byte[48]);
            inStream.nextEntry();
            System.out.println(Short.toUnsignedInt(inStream.getFiles().getShort()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
