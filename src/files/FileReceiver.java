package files;

import files.archiver.tar.TarExtractor;
import net.TransferConnector;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.io.IOUtils;
import tray.UserInterfacing;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class FileReceiver implements Runnable, Cancelable {

    private static int DEFAULT_PORT = 61803;
    private int listenPort;
    private Socket recvSocket;
    private InputStream recvInputStream;
    private OutputStream outputStream;
    private boolean isCancelled;

    //TODO Cancellation?
    private FileReceiver(int port) {
        listenPort = port;
    }

    private FileReceiver() {
        this(DEFAULT_PORT);
    }

    public static FileReceiver receiveStreamRun(OutputStream outputStream, int port) {
        return new FileReceiver(port).setOutputStream(outputStream);
    }

    public static FileReceiver receiveStreamRun(OutputStream outputStream) {
        return receiveStreamRun(outputStream, DEFAULT_PORT);
    }


    public static Thread receiveStream(OutputStream outputStream, int port) {
        FileReceiver receiver = receiveStreamRun(outputStream, port);
        Thread thread = new Thread(receiver, "receiver");
        thread.start();
        return thread;
    }

    public static Thread receiveStream(OutputStream outputStream) {
        return receiveStream(outputStream, DEFAULT_PORT);
    }

    public static FileReceiver receiveFileRun(File dstFile, int port) {
        try {
            if (!dstFile.exists()) dstFile.createNewFile();
            return receiveStreamRun(new FileOutputStream(dstFile), port);
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
        return null;
    }

    public static FileReceiver receiveFileRun(File dstFile) {
        return receiveFileRun(dstFile, DEFAULT_PORT);
    }

    public static Thread receiveFile(File dstFile, int port) {
        try {
            if (!dstFile.exists()) dstFile.createNewFile();
            return receiveStream(new FileOutputStream(dstFile), port);
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
        return null;
    }

    public static Thread receiveFile(File dstFile) {
        return receiveFile(dstFile, DEFAULT_PORT);
    }

    public static FileReceiver receiveTarObj(int port) {
        return new FileReceiver(port);
    }

    public static FileReceiver receiveTarObj() {
        return new FileReceiver();
    }


    public static Thread receiveTar(File base, int port) {
        FileReceiver receiver = new FileReceiver(port);
        Thread thread = new Thread(() -> receiver.runTared(base), "Untar receiver");
        thread.start();
        return thread;
    }

    public static Thread receiveTar(File base) {
        return receiveTar(base, DEFAULT_PORT);
    }

    public static void cancelConnection(int port) {
        FileReceiver receiver = new FileReceiver(port);
        receiver.openConnection();
        receiver.closeConnection();
    }

    public static void cancelConnection() {
        cancelConnection(DEFAULT_PORT);
    }

    private boolean openConnection() {
        try {
            recvSocket = new Socket(TransferConnector.getTarget(), listenPort);
            recvInputStream = recvSocket.getInputStream();
            UserInterfacing.setClipStatus("Local File Pasting");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private InputStream getRecvInputStream() {
        return recvInputStream;
    }

    private FileReceiver setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    private void closeConnection() {
        try {
            if (recvInputStream != null) recvInputStream.close();
            if (recvSocket != null) recvSocket.close();
        } catch (IOException e) {
            UserInterfacing.printError(e);
        }
    }

    @Override
    public synchronized void cancel() {
        if (isCancelled) return;
        isCancelled = true;
        closeConnection();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void run() {
        if (!openConnection()) return;
        try {
            IOUtils.copy(recvInputStream, outputStream);
        } catch (IOException e) {
            if (isCancelled) {
                UserInterfacing.printInfo("File receive cancelled with error: " + e);
            } else UserInterfacing.printError(e);
        }
        closeConnection();
    }

    public List<File> runTared(File base) {
        List<File> files = null;
        if (!openConnection()) return null;
        try {
            recvInputStream = new FramedSnappyCompressorInputStream(recvInputStream);
            files = TarExtractor.decompress(recvInputStream, base);
        } catch (IOException e) {
            if (isCancelled) {
                UserInterfacing.printInfo("File receive cancelled with error: " + e);
            } else UserInterfacing.printError(e);
        }
        closeConnection();
        return files;
    }

    /**
     * Decryption by initialized cipher
     *
     * @param base
     * @param cipher
     */
    public List<File> runTared(File base, Cipher cipher) {
        if (!openConnection()) return null;
        List<File> files = null;
        try {
            recvInputStream = new CipherInputStream(recvInputStream, cipher);
            recvInputStream = new FramedSnappyCompressorInputStream(recvInputStream);
            files = TarExtractor.decompress(recvInputStream, base);
        } catch (IOException e) {
            if (isCancelled) {
                UserInterfacing.printInfo("File receive cancelled with error: " + e);
            } else UserInterfacing.printError(e);
        }
        closeConnection();
        return files;
    }
}
