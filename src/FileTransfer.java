import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Class for receiving files
 */
public class FileTransfer {


    //TODO make this asynchronous
    public List<File> receiveFiles() {
        File dst;
        Path dstFolder;
        List<File> files;
        try {
            dst = File.createTempFile("NetClipboard", ".zip");
            dst.deleteOnExit();
            System.out.println(dst.getAbsolutePath());
            FileReceiver.receiveFileRun(dst);
            dstFolder = Files.createTempDirectory("NetClipboard");
            dstFolder.toFile().deleteOnExit();
            System.out.println(dst.getAbsolutePath());
            files = Decompressor.decompress(dst, dstFolder.toFile());
            dst.delete();
            return files;
            //TODO notify ready to main
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
