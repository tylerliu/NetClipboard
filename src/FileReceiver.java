import java.io.*;
import java.net.Socket;

/**
 * Created by TylerLiu on 2018/03/22.
 */
public class FileReceiver implements Runnable {

    private static int DEFAULT_PORT = 61803;
    private int listenPort;
    private Socket recvSocket;
    private InputStream recvInputStream;
    private OutputStream outputStream;

    private FileReceiver(int port) {
        listenPort = port;
    }

    private FileReceiver() {
        this(DEFAULT_PORT);
    }

    public static Thread receiveStream(OutputStream outputStream, int port) {
        FileReceiver receiver = new FileReceiver(port).setOutputStream(outputStream);
        Thread thread = new Thread(receiver, "receiver");
        thread.start();
        return thread;
    }

    public static Thread receiveStream(OutputStream outputStream) {
        return receiveStream(outputStream, DEFAULT_PORT);
    }

    public static Thread receiveFile(File dstFile, int port) {
        try {
            return receiveStream(new FileOutputStream(dstFile), port);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! " + dstFile.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    public static Thread receiveFile(File dstFile) {
        return receiveFile(dstFile, DEFAULT_PORT);
    }

    public static void main(String[] args) {
        File dst = new File("src.zip");
        try {
            dst.createNewFile();
            receiveFile(dst).join();
            Decompressor.decompress("src.zip", "src_2");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean openConnection() {
        try {
            recvSocket = new Socket(TransferConnector.getTarget(), listenPort);
            recvInputStream = recvSocket.getInputStream();
            System.out.println("receiver connected");
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
            recvInputStream.close();
            recvSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!openConnection()) return;
        try {
            Compressor.copyStream(recvInputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeConnection();
    }
}
