import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by TylerLiu on 2017/10/07.
 */
class Decompressor {

    public static List<File> allFiles;

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

            //make list
            File folder = new File(base + File.separator + entry.getName().substring(0, entry.getName().indexOf('/')));
            if (!files.contains(folder)) files.add(folder);

            File outFile = new File(base + File.separator + entry.getName());   //Define Output Path
            System.out.println("Decompressing file: " + entry.getName());

            //check if the file exist
            if (outFile.exists()) {
                //prepare new name
                String suffix = "";
                String stem = entry.getName();
                if (entry.getName().lastIndexOf('.') > 0 && entry.getName().lastIndexOf('.') > suffix.indexOf(File.separatorChar)) {
                    suffix = entry.getName().substring(entry.getName().lastIndexOf('.'));
                    stem = entry.getName().substring(0, entry.getName().lastIndexOf('.'));
                }

                int index = 1;
                File new_file = outFile;

                while (new_file.exists()) {
                    index++;
                    new_file = new File(base + File.separator + stem + "_" + index + suffix);
                }
                outFile = new_file;
                System.out.println("Decompressing file as: " + stem + "_" + index + suffix);
            }

            allFiles.add(outFile);

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            InputStream input = zipFile.getInputStream(entry);
            OutputStream out = new FileOutputStream(outFile);
            Compressor.copyStream(input, out);
            input.close();
            out.close();
        }

        return files;
    }
}
