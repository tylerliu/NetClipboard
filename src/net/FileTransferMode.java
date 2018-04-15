package net;

public class FileTransferMode {
    private static final Mode DEFAULT = Mode.CHOOSER;
    private static Mode localMode = DEFAULT;
    private static Mode targetMode = DEFAULT;

    public static Mode getTargetMode() {
        return targetMode;
    }

    public static void setTargetMode(Mode targetMode) {
        FileTransferMode.targetMode = targetMode;
    }

    public static Mode getLocalMode() {
        return localMode;
    }

    public static void setLocalMode(Mode localMode) {
        FileTransferMode.localMode = localMode;
    }
    public enum Mode {
        CACHED, //file is cached in a temporary directory
        CHOOSER //file location chosen by a file save dialog
    }

}
