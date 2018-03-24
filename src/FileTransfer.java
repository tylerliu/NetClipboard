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
    private static FileReceiver receiver;
    private static boolean isReceiveScheduled;
    private static boolean isFilesUsed = true;

    public synchronized static void receiveFiles() {
        if (isReceiving()) cancelReceive();
        isReceiveScheduled = true;
        isFilesUsed = true;
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

            receiver = FileReceiver.receiveFileRun(dstZipFile);
            receiver.run();
            receiver = null;

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

            if (dstFolder != null && dstFolder.exists()) {
                dstFolder.delete();
            }
            dstFolder = newDstFolder;
            dstZipFile.delete();
            dstZipFile = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void cancelReceive() {
        isCancelled = true;
        if (isReceiving()) receiver.cancel();
        System.out.println("File receive cancelled");
    }

    private synchronized static boolean isFilesReady() {
        return files != null;
    }

    public synchronized static boolean isReceiveFinished() {
        return files != null;
    }

    public synchronized static boolean isNewlyReceived() {
        return isReceiveFinished() && !isFilesUsed;
    }

    public synchronized static boolean isReceiving() {
        return !isFilesReady() && isReceiveScheduled;
    }

    public synchronized static List<File> getFiles() {
        isFilesUsed = true;
        return files;
    }

    public synchronized static void terminate() {
        if (isReceiving()) cancelReceive();
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
