package files.archiver.tar;

import files.archiver.NamingUtil;

import java.io.IOException;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class CompressTest {
    public static void main(String[] args) throws IOException {
        TarCompressor.compress("./src", "src.tar");
        System.out.println(TarExtractor.decompress("./src.tar", "./src_2", NamingUtil.RenameStrategy.REPLACE));
    }
}
