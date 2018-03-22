import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class FileSender implements Runnable {

    private static int DEFAULT_PORT = 61803;
    private int listenPort;
    private ServerSocket sendServer;
    private Socket sendSocket;
    private OutputStream sendOutputStream;
    private InputStream inputStream;

    private FileSender(int port) {
        listenPort = port;
    }

    private FileSender() {
        this(DEFAULT_PORT);
    }

    public static Thread sendStream(InputStream inputStream, int port) {
        FileSender sender = new FileSender(port).setInputStream(inputStream);
        Thread thread = new Thread(sender, "Sender");
        thread.start();
        return thread;
    }

    public static Thread sendStream(InputStream inputStream) {
        return sendStream(inputStream, DEFAULT_PORT);
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

    public static Thread sendFileList(List<File> files, int port) {
        FileSender sender = new FileSender(port);
        Thread thread = new Thread(() -> {
            if (!sender.openConnection()) return;
            Compressor.compress(files, sender.getSendOutputStream());
            sender.closeConnection();
        }, "Compressed Sender");
        thread.start();
        return thread;
    }

    public static Thread sendFileList(List<File> files) {
        return sendFileList(files, DEFAULT_PORT);
    }

    public static void main(String[] args) {
        try {
            List<File> files = new ArrayList<>();
            files.add(new File("src"));
            sendFileList(files).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
            e.printStackTrace();
        }
        closeConnection();
    }
}
