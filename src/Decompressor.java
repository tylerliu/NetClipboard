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
            System.out.println("Decompressing file: " + entry.getName());
            File outFile = new File(base + File.separator + entry.getName());   // 定义输出的文件路径

            if (!outFile.getParentFile().exists()) outFile.getParentFile().mkdirs(); //make sure directory exist
            if (!outFile.exists()) outFile.createNewFile(); //make sure file exist

            InputStream input = zipFile.getInputStream(entry);
            OutputStream out = new FileOutputStream(outFile);
            Compresser.copyStream(input, out);
            input.close();
            out.close();
        }
    }
}
