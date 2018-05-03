package files.archiver.zip;

import org.apache.commons.io.IOUtils;
import ui.UserInterfacing;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by TylerLiu on 2017/10/06.
 */
public class ZipCompressor {

    /**
     * recursively compress a path
     *
     * @param srcPathName
     * @param dest
     */
    public static void compress(String srcPathName, String dest) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + " does not exist!");
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
            UserInterfacing.printError(e);
        }
    }

    public static void compress(List<File> files, OutputStream dest) {

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
            UserInterfacing.printError(e);
        }
    }

    /**
     * recursively compress a file or directory
     */
    private static void compress(File file, ZipOutputStream out, String basedir) {
        UserInterfacing.printInfo("Compress：" + basedir + file.getName());
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
        try {
            ZipEntry entry = new ZipEntry(basedir + dir.getName());
            out.putNextEntry(entry);
            out.closeEntry();
        } catch (IOException e) {
            UserInterfacing.printError(e);
            throw new RuntimeException(e);
        }
        //noinspection ConstantConditions
        for (File file : dir.listFiles()) {
            compress(file, out, basedir + dir.getName() + "/");
        }
    }

    /**
     * compress a file.
     *
     * @param file    file to compress
     * @param out     files.archiver.zip output stream
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
            IOUtils.copy(is, out);
            is.close();
            out.closeEntry();

        } catch (Exception e) {
            UserInterfacing.printError(e);
            throw new RuntimeException(e);
        }
    }
}
