package zip;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * A decompressor that replace files when there are conflicts
 * Created by TylerLiu on 2017/10/07.
 */
public class ReplaceDecompressor{

    private static List<File> allFiles;

    public static List<File> decompress(String zipPath, String base) throws IOException {
        return decompress(new File(zipPath), new File(base));
    }


    public static List<File> decompress(File zipPath, File base) throws IOException {
        allFiles = new ArrayList<>();
        List<File> files = new ArrayList<>();
        ZipFile zipFile = new ZipFile(zipPath);   // instantiate ZipFile
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipPath));  // instantiate ZipInputStream

        System.out.println(base.getAbsoluteFile());
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) { //iterating through

            String entryName = entry.getName();

            System.out.println("Decompressing file: " + entryName);

            File outFile = new File(base + File.separator + entry); //Define Output Path

            //make List
            allFiles.add(outFile);
            if (entryName.indexOf('/') == -1) {
                files.add(outFile);
            } else { //in a folder
                File folder = new File(base + File.separator + entryName.substring(0, entryName.indexOf('/')));
                if (!files.contains(folder)) files.add(folder);
            }

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            InputStream input = zipFile.getInputStream(entry);
            OutputStream out = new FileOutputStream(outFile);
            Compressor.copyStream(input, out);
            input.close();
            out.close();
        }

        zipFile.close();
        zipInput.close();
        return files;
    }

    public static List<File> getAllFiles() {
        return allFiles;
    }
}
