package files.archiver.zip;

import files.archiver.NamingUtil;

import java.io.IOException;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class CompressTest {
    public static void main(String[] args) throws IOException {
        ZipCompressor.compress("./src", "src.zip");
        System.out.println(ZipExtractor.decompress("./src.zip", "./src_2", NamingUtil.RenameStrategy.REPLACE));
    }
}
