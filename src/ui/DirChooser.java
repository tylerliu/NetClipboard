package ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DirChooser {


    private static File lastSavedDirectory = new File(System.getProperty("user.home"));

    static File chooseSaveDirectory() {
        final AtomicReference<File> selectedDir = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Stage stage = null;
            if (OS.isMac()) {
                stage = new Stage();
                stage.setTitle("Paste File At: ");
                stage.setAlwaysOnTop(true);
                stage.setHeight(1);
                stage.setWidth(710);
                stage.show();
            }
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(lastSavedDirectory);
            selectedDir.set(chooser.showDialog(stage));
            stage.close();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            UserInterfacing.printError(e);
        }

        if (selectedDir.get() != null) {
            return lastSavedDirectory = selectedDir.get();
        } else {
            return null;
        }
    }
}
