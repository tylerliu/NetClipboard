package ui;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DirChooser {


    private static File lastSavedDirectory = new File(System.getProperty("user.home"));

    static File chooseSaveDirectory() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<File> selectedDir = new AtomicReference<>();

        Platform.runLater(() -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(lastSavedDirectory);
            chooser.setTitle("Paste Files...");
            chooser.showDialog(null);
            selectedDir.set(chooser.showDialog(null));
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
