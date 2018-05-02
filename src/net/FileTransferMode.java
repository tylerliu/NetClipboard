package net;

import ui.UserInterfacing;

public class FileTransferMode {
    private static final Mode DEFAULT = Mode.CHOOSER;
    private static Mode localMode = DEFAULT;
    private static Mode targetMode = DEFAULT;
    private static boolean isCurrentModeSent = false;

    public static Mode getTargetMode() {
        return targetMode;
    }

    public static synchronized void setTargetMode(Mode targetMode) {
        UserInterfacing.printInfo("Target Mode Changed");
        FileTransferMode.targetMode = targetMode;
    }

    public static Mode getLocalMode() {
        return localMode;
    }

    public static synchronized void setLocalMode(Mode localMode) {
        if (localMode != FileTransferMode.localMode) {
            FileTransferMode.localMode = localMode;
            isCurrentModeSent = false;
            UserInterfacing.printInfo("Local Mode Changed");
        }
    }

    public static synchronized Mode getModeForSending() {
        isCurrentModeSent = true;
        return localMode;
    }

    public static boolean getIsSent() {
        return isCurrentModeSent;
    }

    public enum Mode {
        CACHED, //file is cached in a temporary directory
        CHOOSER //file location chosen by a file save dialog
    }
}
