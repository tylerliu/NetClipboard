package net;

import filechooser.NativeJFileChooser;
import javax.swing.JFileChooser;

import files.FileReceiver;
import files.FileSender;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for transferring files
 * TODO clean up files
 */
public class FileTransfer {

    private static File lastSavedDirectory;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public synchronized static void receiveFiles() {
        executor.submit(FileTransfer::receiveFilesWorker);
    }

    private static void receiveFilesWorker() {
        int port = PortAllocator.alloc();
        File toDir;

        //choose destination
        //TODO track default directory
        if (lastSavedDirectory == null) {
            lastSavedDirectory = new File(System.getProperty("user.home"));
        }
        NativeJFileChooser chooser = new NativeJFileChooser(lastSavedDirectory);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Paste Files...");

        int chooseResult = chooser.showDialog(null, "Paste");
        if (chooseResult == JFileChooser.APPROVE_OPTION) {
            toDir = chooser.getSelectedFile();
            lastSavedDirectory = toDir;
            System.out.println("Saving to: " + lastSavedDirectory.getAbsolutePath());
        } else {
            System.out.println("Cancelled Pasting.");
            FileReceiver.cancelConnection(port);
            PortAllocator.free(port);
            return;
        }

        System.out.println("File receiving on port: " + port);
        FileReceiver receiver = FileReceiver.receiveTarObj(port);
        receiver.runTared(toDir);
        PortAllocator.free(port);
        System.out.println("File receive done");
    }

    public synchronized static void sendFiles(List<File> sendFiles) {
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles));
    }

    public static void sendFilesWorker(List<File> sendFiles) {
        int port = PortAllocator.alloc();
        System.out.println("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        sender.runTared(sendFiles);
        PortAllocator.free(port);
    }

    public synchronized static void terminate() {
        executor.shutdown();
        while(!executor.isTerminated()){
            System.out.println("Wait for transferring...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
