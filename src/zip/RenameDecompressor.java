package zip;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * A decompressor that rename folders when there are conflicts
 * Created by TylerLiu on 2017/10/07.
 */
public class RenameDecompressor {

    private static List<File> allFiles;
    private static Map<String, String> rootFolder = new HashMap<>();
    private static List<File> rootPaths; //both files and folders

    public static List<File> decompress(String zipPath, String base) throws IOException {
        return decompress(new File(zipPath), new File(base));
    }


    public static List<File> decompress(File zipPath, File base) throws IOException {
        rootFolder.clear();
        rootPaths = new ArrayList<>();
        allFiles = new ArrayList<>();
        ZipFile zipFile = new ZipFile(zipPath);   // instantiate ZipFile
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipPath));  // instantiate ZipInputStream

        System.out.println(base.getAbsoluteFile());
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) { //iterating through

            String entryName = entry.getName();

            System.out.println("Decompressing file: " + entryName);

            File outFile = getUnconflictedFileName(base.toString(), entryName); //Define Output Path

            //make List
            allFiles.add(outFile);

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
        return rootPaths;
    }

    public static List<File> getAllFiles() {
        return allFiles;
    }

    private static String getRootName(String base, String entry) {

        File outFile = new File(base + File.separator + entry);

        //check if the file exist
        if (!outFile.exists()) {
            return entry;
        }

        //prepare new name
        String suffix = "";
        String stem = entry;
        if (entry.lastIndexOf('.') > 0) {
            suffix = entry.substring(entry.lastIndexOf('.'));
            stem = entry.substring(0, entry.lastIndexOf('.'));
        }

        int index = 1;
        File new_file = outFile;

        while (new_file.exists()) {
            index++;
            new_file = new File(base + File.separator + stem + "_" + index + suffix);
        }

        return stem + "_" + index + suffix;
    }

    private static File getUnconflictedFileName(String base, String entry) {
        if (entry.indexOf('/') == -1) { //file itself
            File file = new File(base + File.separator + getRootName(base, entry));
            rootPaths.add(file);
            return file;
        }

        String stemFolder = entry.substring(0, entry.indexOf('/'));

        if (!rootFolder.containsKey(stemFolder)) {
            rootFolder.put(stemFolder, getRootName(base, stemFolder));
            rootPaths.add(new File(base + File.separator + rootFolder.get(stemFolder)));
        }

        return new File(base + File.separator + rootFolder.get(stemFolder) + entry.substring(entry.indexOf('/')));
    }
}
