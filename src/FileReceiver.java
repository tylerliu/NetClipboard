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

    //TODO Cancellation?
    private FileReceiver(int port) {
        listenPort = port;
    }

    private FileReceiver() {
        this(DEFAULT_PORT);
    }

    public static Runnable receiveStreamRun(OutputStream outputStream, int port){
        return new FileReceiver(port).setOutputStream(outputStream);
    }

    public static Runnable receiveStreamRun(OutputStream outputStream) {
        return receiveStreamRun(outputStream, DEFAULT_PORT);
    }


    public static Thread receiveStream(OutputStream outputStream, int port) {
        Runnable receiver = receiveStreamRun(outputStream, port);
        Thread thread = new Thread(receiver, "receiver");
        thread.start();
        return thread;
    }

    public static Thread receiveStream(OutputStream outputStream) {
        return receiveStream(outputStream, DEFAULT_PORT);
    }

    public static Runnable recriveFileRun(File dstFile, int port) {
        try {
            return receiveStreamRun(new FileOutputStream(dstFile), port);
        } catch (FileNotFoundException e) {
            System.out.println("File not found! " + dstFile.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    public static Runnable receiveFileRun(File dstFile) {
        return recriveFileRun(dstFile, DEFAULT_PORT);
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
