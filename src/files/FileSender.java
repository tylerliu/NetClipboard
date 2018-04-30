package files;

import files.archiver.tar.TarCompressor;
import net.TransferConnector;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import tray.Interfacing;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class FileSender implements Runnable, Cancelable {

    private static int DEFAULT_PORT = 61803;
    private int listenPort;
    private ServerSocket sendServer;
    private Socket sendSocket;
    private OutputStream sendOutputStream;
    private InputStream inputStream;
    private boolean isCancelled;

    private FileSender(int port) {
        listenPort = port;
    }

    private FileSender() {
        this(DEFAULT_PORT);
    }

    public static FileSender sendStreamRun(InputStream inputStream, int port) {
        return new FileSender(port).setInputStream(inputStream);
    }

    public static FileSender sendStreamRun(InputStream inputStream) {
        return sendStreamRun(inputStream, DEFAULT_PORT);
    }

    public static Thread sendStream(InputStream inputStream, int port) {
        Thread thread = new Thread(sendStream(inputStream, port), "Sender");
        thread.start();
        return thread;
    }

    public static Thread sendStream(InputStream inputStream) {
        return sendStream(inputStream, DEFAULT_PORT);
    }

    public static FileSender sendFileRun(File file, int port) {
        try {
            return sendStreamRun(new FileInputStream(file), port);
        } catch (FileNotFoundException e) {
            Interfacing.printInfo("File not found! " + file.getAbsolutePath());
            Interfacing.printError(e);
        }
        return null;
    }

    public static FileSender sendFileRun(File file) {
        return sendFileRun(file, DEFAULT_PORT);
    }

    public static Thread sendFile(File file, int port) {
        try {
            return sendStream(new FileInputStream(file), port);
        } catch (FileNotFoundException e) {
            Interfacing.printInfo("File not found! " + file.getAbsolutePath());
            Interfacing.printError(e);
        }
        return null;
    }

    public static Thread sendFile(File file) {
        return sendFile(file, DEFAULT_PORT);
    }


    public static FileSender sendFileListObj(int port) {
        return new FileSender(port);
    }

    public static FileSender sendFileListObj() {
        return new FileSender();
    }

    public static Thread sendFileList(List<File> files, int port) {
        FileSender sender = new FileSender(port);
        Thread thread = new Thread(() -> sender.runTared(files), "Compressed Sender");
        thread.start();
        return thread;
    }

    public static Thread sendFileList(List<File> files) {
        return sendFileList(files, DEFAULT_PORT);
    }

    private boolean openConnection() {
        try {
            sendServer = new ServerSocket(listenPort);
            sendSocket = sendServer.accept();
            if (!sendSocket.getInetAddress().equals(TransferConnector.getTarget())) {
                Interfacing.printInfo("Wrong connection: " + sendSocket.getInetAddress());
                sendSocket.close();
                sendSocket = sendServer.accept();
            }
            sendOutputStream = sendSocket.getOutputStream();
            Interfacing.setClipStatus("Local File Sending");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private OutputStream getSendOutputStream() {
        return sendOutputStream;
    }

    private FileSender setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
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

    private void closeConnection() {
        try {
            if (sendOutputStream != null) sendOutputStream.close();
            if (sendSocket != null) sendSocket.close();
            if (sendServer != null) sendServer.close();
        } catch (IOException e) {
            Interfacing.printError(e);
        }
    }

    @Override
    public void run() {
        if (!openConnection()) return;
        try {
            IOUtils.copy(inputStream, sendOutputStream);
        } catch (IOException e) {
            if (isCancelled) {
                Interfacing.printInfo("File send cancel with error: " + e);
            } else Interfacing.printError(e);
        }
        closeConnection();
    }

    public void runTared(List<File> files) {
        if (!openConnection()) return;
        try {
            sendOutputStream = new FramedSnappyCompressorOutputStream(sendOutputStream);
            TarCompressor.compress(files, getSendOutputStream());
        } catch (Exception e) {
            if (isCancelled) {
                Interfacing.printInfo("File send cancel with error: " + e);
                return;
            }
            Interfacing.printError(e);
        }
        closeConnection();
    }

    /**
     * Encryption by initialized cipher
     *
     * @param files
     * @param cipher the initialized cipher to be used
     */
    public void runTared(List<File> files, Cipher cipher) {
        if (!openConnection()) return;
        try {
            sendOutputStream = new CipherOutputStream(sendOutputStream, cipher);
            sendOutputStream = new FramedSnappyCompressorOutputStream(sendOutputStream);
            TarCompressor.compress(files, getSendOutputStream());
        } catch (Exception e) {
            if (isCancelled) {
                Interfacing.printInfo("File send cancel with error: " + e);
                return;
            }
            Interfacing.printError(e);
        }

        closeConnection();
    }
}
