package clip.c;

import clip.MacFilesClipboard;
import org.apache.commons.io.FileUtils;
import tray.Interfacing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class macClipboardNative {

    private static macClipboardNative instance;

    private macClipboardNative() {
        assert MacFilesClipboard.isMac();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("NetClipboard", ".dylib");
            tempFile.deleteOnExit();
            System.out.println("Temp Library file: " + tempFile.getCanonicalPath());
        } catch (IOException e) {
            Interfacing.printError(e);
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/resources/libClipboardJNI.dylib")) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            FileUtils.deleteQuietly(tempFile);
            Interfacing.printError(e);
        } catch (NullPointerException e) {
            FileUtils.deleteQuietly(tempFile);
            new FileNotFoundException("File " + "\"/libClipboardJNI.dylib\"" + " was not found inside JAR.").printStackTrace();
        }
        try {
            System.load(tempFile.getAbsolutePath());
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
        /*
        try {
            System.load(new File("./native/libClipboardJNI.dylib").getCanonicalPath());
        } catch (IOException e) {
            Interfacing.printError(e);
        }
        */
    }

    private native void setClipboardFiles(String[] files);

    public static void setClipboardFiles(List<File> files) {
        //check os
        if (!MacFilesClipboard.isMac()) {
            System.out.println("Clipboard: this is not Mac!");
            return;
        }
        if (instance == null) instance = new macClipboardNative();

        String[] filePaths = new String[files.size()];
        for (int i = 0; i < filePaths.length; i ++) {
            try {
                filePaths[i] = files.get(i).getCanonicalPath();
            } catch (IOException e) {
                Interfacing.printError(e);
            }
        }

        instance.setClipboardFiles(filePaths);
    }
}