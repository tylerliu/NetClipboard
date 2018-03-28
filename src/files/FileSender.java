package files;

import net.TransferConnector;
import org.apache.commons.io.IOUtils;
import files.zip.Compressor;

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

    //TODO Cancellation?
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
            System.out.println("File not found! " + file.getAbsolutePath());
            e.printStackTrace();
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
            System.out.println("File not found! " + file.getAbsolutePath());
            e.printStackTrace();
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
        Thread thread = new Thread(() -> sender.runCompressed(files), "Compressed Sender");
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
                System.out.println("Wrong connection: " + sendSocket.getInetAddress());
                sendSocket.close();
                sendSocket = sendServer.accept();
            }
            sendOutputStream = sendSocket.getOutputStream();
            System.out.println("Sender connected");
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
        isCancelled = true;
        closeConnection();
    }

    private void closeConnection() {
        try {
            if (sendOutputStream != null) sendOutputStream.close();
            if (sendSocket != null) sendSocket.close();
            if (sendServer != null) sendServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!openConnection()) return;
        try {
            IOUtils.copy(inputStream, sendOutputStream);
        } catch (IOException e) {
            if (isCancelled) {
                System.out.println("File send cancel with error" + e);
            } else e.printStackTrace();
        }
        closeConnection();
        System.out.println("File send done");
    }

    public void runCompressed(List<File> files) {
        if (!openConnection()) return;
        Compressor.compress(files, getSendOutputStream());
        if (isCancelled) {
            System.out.println("File send cancel with error");
            return;
        }
        closeConnection();
        System.out.println("File send done");
    }
}
