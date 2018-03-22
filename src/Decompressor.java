import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by TylerLiu on 2017/10/07.
 */
class Decompressor {

    public static void decompress(String zipPath, String base) throws IOException {
        decompress(new File(zipPath), new File(base));
    }


    private static void decompress(File zipPath, File base) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath);   // 实例化ZipFile对象
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipPath));  // 实例化ZIpInputStream

        System.out.println(base.getAbsoluteFile());
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) { //run through

            File outFile = new File(base + File.separator + entry.getName());   // 定义输出的文件路径
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


            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            InputStream input = zipFile.getInputStream(entry);
            OutputStream out = new FileOutputStream(outFile);
            Compressor.copyStream(input, out);
            input.close();
            out.close();
        }
    }
}
