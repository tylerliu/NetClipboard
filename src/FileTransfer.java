import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for receiving files
 */
public class FileTransfer {

    private static File dstZipFile;
    private static File dstFolder;
    private static List<File> files = null;
    private static boolean isCancelled;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Cancelable transferConnector;
    private static boolean isReceiveScheduled;
    private static boolean isFilesUsed = true;
    private static boolean isFinished;

    public synchronized static void receiveFiles() {
        if (isTransfering()) cancelTransfer();
        isReceiveScheduled = true;
        isFilesUsed = true;
        isCancelled = false;
        isFinished = false;
        executor.submit(FileTransfer::receiveFilesWorker);
    }

    private static void receiveFilesWorker() {
        try {
            files = null;
            if (dstZipFile != null && dstZipFile.exists()) {
                dstZipFile.delete();
            }
            dstZipFile = File.createTempFile("NetClipboard", ".zip");
            dstZipFile.deleteOnExit();
            System.out.println("Receive Zip: " + dstZipFile.getAbsolutePath());

            if (isCancelled) {
                dstZipFile.delete();
                dstZipFile = null;
                return;
            }

            FileReceiver receiver = FileReceiver.receiveFileRun(dstZipFile);
            transferConnector = receiver;
            receiver.run();
            transferConnector = null;

            File newDstFolder = Files.createTempDirectory("NetClipboard").toFile();
            newDstFolder.deleteOnExit();
            System.out.println("Receive Folder: " + newDstFolder.getAbsolutePath());

            if (isCancelled) {
                newDstFolder.delete();
                dstZipFile.delete();
                dstZipFile = null;
                return;
            }

            files = Decompressor.decompress(dstZipFile, newDstFolder);
            isFilesUsed = false;
            isFinished = true;

            if (dstFolder != null && dstFolder.exists()) {
                dstFolder.delete();
            }
            dstFolder = newDstFolder;
            dstZipFile.delete();
            dstZipFile = null;
            System.out.println("File receive done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void sendFiles(List<File> sendFiles) {
        if (isTransfering()) cancelTransfer();
        files = sendFiles;
        isReceiveScheduled = true;
        isFilesUsed = true;
        isCancelled = false;
        isFinished = false;
        executor.submit(FileTransfer::sendFilesWorker);
    }

    public static void sendFilesWorker() {
        FileSender sender = FileSender.sendFileListObj();
        transferConnector = sender;
        sender.runCompressed(files);
        isFinished = true;
    }

    public synchronized static void cancelTransfer() {
        if (isCancelled) return;
        isCancelled = true;
        if (isTransfering() && transferConnector != null) {
            transferConnector.cancel();
        }
        System.out.println("File receive cancelled");
    }

    public synchronized static boolean isTransferFinished() {
        return isFinished;
    }

    public synchronized static boolean isNewlyReceived() {
        return isFinished && !isFilesUsed;
    }

    public synchronized static boolean isTransfering() {
        return !isFinished && isReceiveScheduled;
    }

    public synchronized static List<File> getFiles() {
        isFilesUsed = true;
        return files;
    }

    public synchronized static void terminate() {
        if (isTransfering()) cancelTransfer();
        executor.shutdown();
    }

    //TODO add String flavor?
    public static class FileTransferable implements Transferable, ClipboardOwner {

        private List<File> listOfFiles;

        public FileTransferable(List<File> listOfFiles) {
            this.listOfFiles = listOfFiles;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported(flavor)) {
                return listOfFiles;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {

        }
    }
}
