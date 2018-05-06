package files.archiver.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import ui.UserInterfacing;

import java.io.*;
import java.util.List;

/**
 * Created by TylerLiu on 2017/10/06.
 */
public class TarCompressor {

    /**
     * recursively compress a path
     *
     * @param srcPathName source path of the files
     * @param dest        the destination file
     */
    public static void compress(String srcPathName, String dest) {
        File file = new File(srcPathName);
        if (!file.exists())
            throw new RuntimeException(srcPathName + " does not exist!");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dest));
            TarArchiveOutputStream out = new TarArchiveOutputStream(fileOutputStream);
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

        TarArchiveOutputStream out = new TarArchiveOutputStream(dest, "UTF8");
        // TAR has an 8 gig file limit by default, this gets around that
        out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        out.setAddPaxHeadersForNonAsciiNames(true);
        String basedir = "";

        for (File file : files) {
            if (!file.exists())
                System.err.println(file.getName() + " does not exist！");
            compress(file, out, basedir);
        }

        try {
            out.finish();
            out.close();
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
    }

    /**
     * recursively compress a file or directory
     */
    private static void compress(File file, TarArchiveOutputStream out, String basedir) {
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
    private static void compressDirectory(File dir, TarArchiveOutputStream out, String basedir) {
        if (!dir.exists())
            return;

        try {
            ArchiveEntry entry = out.createArchiveEntry(dir, basedir + dir.getName());
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
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
    private static void compressFile(File file, TarArchiveOutputStream out, String basedir) {
        if (!file.exists()) {
            return;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            ArchiveEntry entry = out.createArchiveEntry(file, basedir + file.getName());
            out.putArchiveEntry(entry);
            IOUtils.copy(is, out);
            is.close();
            out.closeArchiveEntry();
        } catch (Exception e) {
            UserInterfacing.printError(e);
            throw new RuntimeException(e);
        }
    }
}
