package zip;

import java.io.IOException;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class CompressTest {
    public static void main(String[] args) throws IOException {
        Compressor.compress("./NetworkClipboard.iml", "src.zip");
        System.out.println(RenameDecompressor.decompress("./src.zip", "./src_2"));
    }
}
