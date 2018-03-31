package files.archiver.tar;

import files.archiver.NamingUtil;
import files.archiver.NamingUtil.RenameStrategy;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static files.archiver.NamingUtil.RenameStrategy.RENAME_ROOT;

/**
 * A decompressor that replace files when there are conflicts
 * Implemented with apache compress
 * Created by Tyler Liu on 2017/10/07.
 */
public class TarExtractor {

    public static List<File> decompress(String tarPath, String base, RenameStrategy strategy) throws IOException {
        return decompress(new FileInputStream(tarPath), new File(base), strategy);
    }

    public static List<File> decompress(String tarPath, String base) throws IOException {
        return decompress(tarPath, base, RENAME_ROOT);
    }

    public static List<File> decompress(File tarPath, File base, RenameStrategy strategy) throws IOException {
        return decompress(new FileInputStream(tarPath), base, strategy);
    }


    public static List<File> decompress(File tarPath, File base) throws IOException {
        return decompress(tarPath, base, RENAME_ROOT);
    }

    public static List<File> decompress(InputStream tarIn, File base) throws IOException {
        return decompress(tarIn, base, RENAME_ROOT);
    }

    public static List<File> decompress(InputStream tarIn, File base, RenameStrategy strategy) throws IOException {
        NamingUtil util = new NamingUtil(strategy);
        TarArchiveInputStream tarInput = new TarArchiveInputStream(tarIn,"UTF8"); // instantiate TarInputStream
        TarArchiveEntry entry;
        while ((entry = tarInput.getNextTarEntry()) != null) { //iterating through

            String entryName = entry.getName();

            if (entryName.endsWith("/")) continue;

            System.out.println("Decompressing file: " + entryName);

            File outFile = util.getUnconflictFileName(base.toString(), entryName); //Define Output Path

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            FileOutputStream out = new FileOutputStream(outFile);
            IOUtils.copy(tarInput, out);
            out.close();
        }

        tarInput.close();
        return util.getRootPaths();
    }
}
