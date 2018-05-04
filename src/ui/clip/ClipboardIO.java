package ui.clip;

import format.DataFormat;
import javafx.application.Platform;
import javafx.scene.input.ClipboardContent;
import ui.UserInterfacing;

import javafx.scene.input.Clipboard;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by TylerLiu on 2017/03/22.
 */
public class ClipboardIO {

    private static ClipboardContent lastContent;
    private static boolean isFromRemote;

    /**
     * check for new things in the clipboard
     *
     * @return true if change happens
     */
    public static synchronized boolean checkNew() {
        ClipboardContent content = getClipContent();
        byte type = getType(content);
        if (type == DataFormat.NULL) return false;
        switch (type) {
            case DataFormat.FILES:
                List<File> files = content.getFiles();
                if (isNewFiles(files)) {
                    lastContent = content;
                    isFromRemote = false;
                    UserInterfacing.printInfo("Local Clipboard New: " + files);
                    UserInterfacing.setClipStatus("Local Files");
                    return true;
                }
                break;
            case DataFormat.STRING:
                String n = content.getString();
                if (isNewString(n)) {
                    lastContent = content;
                    isFromRemote = false;
                    UserInterfacing.printInfo("Local Clipboard New: " + n);
                    if (n.contains("\n")) n = n.substring(0, n.indexOf('\n')) + "...";
                    UserInterfacing.setClipStatus("Local: " + (n.length() > 30 ? n.substring(0, 30) + "..." : n));
                    return true;
                }
                break;
            default:
        }
        return false;
    }

    private static boolean isNewString(String data) {
        return data != null && (lastContent == null || DataFormat.STRING != getLastType() || !lastContent.getString().equals(data));
    }

    private static boolean isNewFiles(List<File> data) {
        return data != null && (lastContent == null || DataFormat.FILES != getLastType() || !lastContent.getFiles().equals(data));
    }

    //TODO support Image, html?
    public static byte getLastType() {
        return getType(lastContent);
    }

    private static byte getType(ClipboardContent content) {
        if (content.hasFiles()) return DataFormat.FILES;
        if (content.hasString()) return DataFormat.STRING;
        return DataFormat.NULL;
    }

    public static ClipboardContent getLastContent() {
        return lastContent;
    }

    public static boolean isLastFromRemote() {
        return isFromRemote;
    }

    public static synchronized void setSysClipboardText(String s) {
        isFromRemote = true;
        lastContent = new ClipboardContent();
        lastContent.putString(s);
        setClipContent(lastContent);
    }

    public static synchronized void setSysClipboardFiles(List<File> files) {
        isFromRemote = true;
        lastContent = new ClipboardContent();
        lastContent.putFiles(files);
        setClipContent(lastContent);
    }

    public static synchronized void unsetSysClipboard() {
        isFromRemote = true;
        lastContent = new ClipboardContent();
        lastContent.putString("");
        setClipContent(lastContent);
    }

    private static ClipboardContent getClipContent() {
        ClipboardContent content = new ClipboardContent();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            for (javafx.scene.input.DataFormat format : Clipboard.getSystemClipboard().getContentTypes()) {
                content.put(format, Clipboard.getSystemClipboard().getContent(format));
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            UserInterfacing.printError(e);
        }

        return content;
    }

    private static void setClipContent(ClipboardContent content) {
        Platform.runLater(() -> Clipboard.getSystemClipboard().setContent(content));
    }
}
