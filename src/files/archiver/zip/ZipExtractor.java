package files.archiver.zip;

import files.archiver.NamingUtil;
import files.archiver.NamingUtil.RenameStrategy;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static files.archiver.NamingUtil.RenameStrategy.RENAME_ROOT;

/**
 * A decompressor that replace files when there are conflicts
 * Created by TylerLiu on 2017/10/07.
 */
public class ZipExtractor {

    public static List<File> decompress(String zipPath, String base, RenameStrategy strategy) throws IOException {
        return decompress(new File(zipPath), new File(base), strategy);
    }

    public static List<File> decompress(String zipPath, String base) throws IOException {
        return decompress(zipPath, base, RENAME_ROOT);
    }

    public static List<File> decompress(File zipPath, File base) throws IOException {
        return decompress(zipPath, base, RENAME_ROOT);
    }

    public static List<File> decompress(File zipPath, File base, RenameStrategy strategy) throws IOException {
        NamingUtil namingUtil = new NamingUtil(strategy);
        ZipFile zipFile = new ZipFile(zipPath);   // instantiate ZipFile
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipPath));  // instantiate ZipInputStream

        System.out.println(base.getAbsoluteFile());
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) { //iterating through

            String entryName = entry.getName();

            System.out.println("Decompressing file: " + entryName);

            File outFile = namingUtil.getUnconflictFileName(base.toString(), entryName); //Define Output Path

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            InputStream input = zipFile.getInputStream(entry);
            OutputStream out = new FileOutputStream(outFile);
            IOUtils.copy(input, out);
            input.close();
            out.close();
        }

        zipInput.close();
        zipFile.close();
        return namingUtil.getRootPaths();
    }

}
