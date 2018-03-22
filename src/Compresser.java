import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by TylerLiu on 2017/10/06.
 */
class Compresser {

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * recursively compress a path
     *
     * @param srcPathName
     * @param dest
     */
    public static void compress(String srcPathName, String dest) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dest));
            ZipOutputStream out = new ZipOutputStream(fileOutputStream);
            String basedir = "";
            compress(file, out, basedir);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void compress(List<File> files, String dest) {

        try {
            compress(files, new FileOutputStream(new File(dest)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void compress(List<File> files, OutputStream dest) {

        ZipOutputStream out = new ZipOutputStream(dest);
        String basedir = "";

        for (File file : files) {
            if (!file.exists())
                System.err.println(file.getName() + " does not exist！");
            compress(file, out, basedir);
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * recursively compress a file or directory
     *
     * @param file
     * @param out
     * @param basedir
     */
    private static void compress(File file, ZipOutputStream out, String basedir) {
        System.out.println("Compress：" + basedir + file.getName());
        //categorize
        if (file.isDirectory()) {
            compressDirectory(file, out, basedir);
        } else {
            compressFile(file, out, basedir);
        }
    }

    /**
     * compress a directory
     */
    private static void compressDirectory(File dir, ZipOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        //noinspection ConstantConditions
        for (File file : dir.listFiles()) {
            /* 递归 */
            compress(file, out, basedir + dir.getName() + "/");
        }
    }

    /**
     * compress a file.
     *
     * @param file    file to compress
     * @param out     zip output stream
     * @param basedir base directory of the file
     */
    private static void compressFile(File file, ZipOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            copyStream(is, out);
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
