import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class FileSender implements Runnable {

    public static FileSender lastSender;
    private static int DEFAULT_PORT = 61803;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
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

    public static ExecutorService sendStream(InputStream inputStream, int port) {
        FileSender sender = new FileSender(port).setInputStream(inputStream);
        executor.submit(sender);
        lastSender = sender;
        return executor;
    }

    public static ExecutorService sendStream(InputStream inputStream) {
        return sendStream(inputStream, DEFAULT_PORT);
    }

    public static ExecutorService sendFile(File file, int port) {
        try {
            return sendStream(new FileInputStream(file), port);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    public static ExecutorService sendFile(File file) {
        return sendFile(file, DEFAULT_PORT);
    }

    public static ExecutorService sendFileList(List<File> files, int port) {
        FileSender sender = new FileSender(port);
        executor.submit(() -> {
            if (!sender.openConnection()) return;
            Compressor.compress(files, sender.getSendOutputStream());
            sender.closeConnection();
        }, "Compressed Sender");
        lastSender = sender;
        return executor;
    }

    public static ExecutorService sendFileList(List<File> files) {
        return sendFileList(files, DEFAULT_PORT);
    }

    public static void terminate() {
        lastSender.cancel();
        executor.shutdown();
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

    public synchronized void cancel() {
        isCancelled = true;
        closeConnection();
    }

    private void closeConnection() {
        try {
            sendOutputStream.close();
            sendSocket.close();
            sendServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!openConnection()) return;
        try {
            Compressor.copyStream(inputStream, sendOutputStream);
        } catch (IOException e) {
            if (isCancelled) {
                System.out.println("File send cancelled");
            }
            e.printStackTrace();
        }
        closeConnection();
        System.out.println("File send done");
    }
}
