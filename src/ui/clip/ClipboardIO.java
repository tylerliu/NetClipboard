package ui.clip;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import ui.OS;
import ui.UserInterfacing;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
    public static boolean checkNew() {
        ClipboardContent content = getClipContent();
        if (!isNewContent(content)) return false;
        lastContent = content;
        isFromRemote = false;
        ContentUtil.printContent(content, "Local");
        return true;
    }

    private static boolean isNewContent(ClipboardContent data) {
        return data != null && (lastContent == null || !ContentUtil.isContentEqual(lastContent, data));
    }

    public static ClipboardContent getLastContent() {
        return lastContent;
    }

    public static boolean isLastFromRemote() {
        return isFromRemote;
    }

    public static synchronized void setSysClipboardContent(ClipboardContent content) {
        isFromRemote = true;
        lastContent = content;
        setClipContent(content);
    }

    public static synchronized void unsetSysClipboard() {
        isFromRemote = true;
        lastContent = new ClipboardContent();
        lastContent.putString("");
        setClipContent(lastContent);
    }

    private synchronized static ClipboardContent getClipContent() {
        ClipboardContent content = new ClipboardContent();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            //get only supported content
            if (clipboard.hasString()) content.putString(clipboard.getString());
            if (clipboard.hasHtml()) content.putHtml(clipboard.getHtml());
            if (clipboard.hasRtf()) content.putRtf(clipboard.getRtf());
            if (clipboard.hasUrl()) content.putUrl(clipboard.getUrl());
            if (clipboard.hasFiles()) content.putFiles(clipboard.getFiles());

            /*for (javafx.scene.input.DataFormat format : Clipboard.getSystemClipboard().getContentTypes()) {
                    content.put(format, Clipboard.getSystemClipboard().getContent(format));
            }*/
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            UserInterfacing.printError(e);
        }

        //pre-process content
        if (content.hasFiles() && !content.hasString()) {
            String name = content.getFiles().get(0).getAbsolutePath();
            while (name.charAt(name.length() - 1) == File.separatorChar) name = name.substring(0, name.length() - 1);
            name = name.substring(name.lastIndexOf(File.separatorChar) + 1);
            content.putString(name);
        }

        if (content.hasUrl()) {
            try {
                new URI(content.getUrl());
            } catch (URISyntaxException ex) {
                content.remove(DataFormat.URL);
            }
        }
        return content;
    }

    private static synchronized void setClipContent(ClipboardContent content) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Clipboard.getSystemClipboard().setContent(content);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
