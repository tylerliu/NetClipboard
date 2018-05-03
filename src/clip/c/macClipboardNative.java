package clip.c;

import org.apache.commons.io.FileUtils;
import ui.OS;
import ui.UserInterfacing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class macClipboardNative {

    private static macClipboardNative instance;

    private macClipboardNative() {
        assert OS.isMac();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("NetClipboard", ".dylib");
            tempFile.deleteOnExit();
            UserInterfacing.printInfo("Temp Library file: " + tempFile.getCanonicalPath());
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }

        try (InputStream inputStream = getClass().getResourceAsStream("/resources/libClipboardJNI.dylib")) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            FileUtils.deleteQuietly(tempFile);
            UserInterfacing.printError(e);
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
            UserInterfacing.printError(e);
        }
        */
    }

    public static void setClipboardFiles(List<File> files) {
        //check os
        if (!OS.isMac()) {
            UserInterfacing.printInfo("Clipboard: this is not Mac!");
            return;
        }
        if (instance == null) instance = new macClipboardNative();

        String[] filePaths = new String[files.size()];
        for (int i = 0; i < filePaths.length; i++) {
            try {
                filePaths[i] = files.get(i).getCanonicalPath();
            } catch (IOException e) {
                UserInterfacing.printError(e);
            }
        }

        instance.setClipboardFiles(filePaths);
    }

    private native void setClipboardFiles(String[] files);
}