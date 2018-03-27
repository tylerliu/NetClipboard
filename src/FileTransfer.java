import FileChooser.NativeJFileChooser;
import javax.swing.JFileChooser;

import org.apache.commons.io.FileDeleteStrategy;
import zip.RenameDecompressor;
import java.io.File;
import java.io.IOException;
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
        try {
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

            File dstZipFile = File.createTempFile("NetClipboard", ".zip");
            dstZipFile.deleteOnExit();
            System.out.println("Receive Zip: " + dstZipFile.getAbsolutePath());

            System.out.println("File receiving on port: " + port);

            FileReceiver receiver = FileReceiver.receiveFileRun(dstZipFile, port);
            receiver.run();

            PortAllocator.free(port);

            RenameDecompressor.decompress(dstZipFile, toDir);

            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FileDeleteStrategy.FORCE.delete(dstZipFile);

            System.out.println("File receive done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void sendFiles(List<File> sendFiles) {
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles));
    }

    public static void sendFilesWorker(List<File> sendFiles) {
        int port = PortAllocator.alloc();
        System.out.println("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        sender.runCompressed(sendFiles);
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
