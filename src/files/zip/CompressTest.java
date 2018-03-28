package files.zip;

import java.io.IOException;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class CompressTest {
    public static void main(String[] args) throws IOException {
        Compressor.compress("./src", "src.files.zip");
        System.out.println(CombineDecompressor.decompress("./src.files.zip", "./src_2"));
    }
}
