package net;

import main.ClipboardIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles the network connection for clipboard sharing
 */
public class TransferConnector {

    private static final boolean isLoopBack = false;
    private static boolean isServer;
    private static final int connectionPort = 31415;
    private static MultipleFormatInStream inStream;
    private static MultipleFormatOutStream outStream;
    private static Socket socket;
    private static ServerSocket serverSocket;
    private static boolean terminateInitiated;

    public static InetAddress getTarget() {
        if (isLoopBack) return InetAddress.getLoopbackAddress();
            // TODO BroadCasting maybe?
        else {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                if (Objects.equals(localHost.getHostAddress(), "192.168.1.3"))
                    return InetAddress.getByName("192.168.1.7");
                if (Objects.equals(localHost.getHostAddress(), "192.168.1.7"))
                    return InetAddress.getByName("192.168.1.3");
            } catch (UnknownHostException u) {
                u.printStackTrace();
            }

        }

        return null;
    }

    /**
     * determine if this is the server side of the connection
     */
    public static void checkServer() {
        try {
            isServer = Arrays.compare(InetAddress.getLocalHost().getAddress(), getTarget().getAddress()) > 0;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public static void connect() {
        try {
            checkServer();
            System.out.println("This computer is " + InetAddress.getLocalHost().toString());
            System.out.println("This is configured as " + (isServer ? "Server. " : "Client. "));
            if (isServer) {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(connectionPort));
                socket = serverSocket.accept();
                System.out.println("Server Connected");
            } else {
                socket = new Socket(getTarget(), connectionPort);
                System.out.println("Client Connected");
            }

            inStream = new MultipleFormatInStream(socket.getInputStream());
            outStream = new MultipleFormatOutStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void DataTransferExecute() {
        Thread thread = new Thread(TransferConnector::writer, "writer");
        thread.start();
        //read thread
        reader();
    }

    public static void writer() {
        try {
            while (true) {

                //check clipboard
                if (ClipboardIO.checkNew()) {
                    switch (ClipboardIO.getLastType()) {
                        case STRING:
                            outStream.writeString(ClipboardIO.getLastString());
                            break;
                        case HTML:
                        case FILES:
                            outStream.writeFiles();
                            FileTransfer.sendFiles(ClipboardIO.getLastFiles());
                            break;
                        case END:
                            return;
                        default:
                    }
                }

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {

                }
            }

        } catch (IOException e) {
            //do nothing
        }
    }

    public static void reader() {
        //read
        try {
            while (true) {
                Object[] b = inStream.readNext();
                if (b == null) System.exit(0);
                switch (ClipboardIO.getContentType((int) b[0])) {
                    case STRING:
                        String s = (String) b[1];
                        System.out.println("Remote Clipboard New: " + s);
                        ClipboardIO.setSysClipboardText(s);
                        break;
                    case END:
                        terminateInitiated = true;
                        return;
                    case HTML:
                    case FILES:
                        FileTransfer.receiveFiles();
                        ClipboardIO.unsetSysClipboard();
                    default:
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if (!terminateInitiated && socket != null && socket.isConnected()) {
                terminateInitiated = true;
                outStream.writeEND();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
            if (socket != null)
                socket.close();
            if (isServer && serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
