package clip.c;

import clip.MacFilesClipboard;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class macClipboardNative {

    private static macClipboardNative instance;

    private macClipboardNative() {
        assert MacFilesClipboard.isMac();
        try {
            System.load(new File("./native/libClipboardJNI.dylib").getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                e.printStackTrace();
            }
        }

        instance.setClipboardFiles(filePaths);
    }
}