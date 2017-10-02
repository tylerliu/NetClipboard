import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TylerLiu on 2017/08/28.
 */
public class TransferConnector{

    static final boolean isLoopBack = false;
    static final int connectionPort = 31415;
    static MultipleFormatInputStream inputStream;
    static MultipleFormatOutputStream outputStream;
    static Socket socket;
    static ServerSocket serverSocket;
    static InetAddress localHost;
    private static AtomicBoolean isConnOpen = new AtomicBoolean(false);

    static InetAddress getTarget(){
        if (isLoopBack) return InetAddress.getLoopbackAddress();
        else {
            try{
                localHost = InetAddress.getLocalHost();
                if (Objects.equals(localHost.getHostAddress(), "192.168.1.3")) return InetAddress.getByName("192.168.1.7");
                if (Objects.equals(localHost.getHostAddress(), "192.168.1.7")) return InetAddress.getByName("192.168.1.3");
            } catch (UnknownHostException u){
                u.printStackTrace();
            }

        }

        return null;
    }


    static void connect(){
        Thread serverThread = new Thread(TransferConnector::serverConn);
        Thread clientThread = new Thread(TransferConnector::clientConn);
        clientThread.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (clientThread.isAlive())
            serverThread.start();
        try {
            while (clientThread.isAlive() && serverThread.isAlive()) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert inputStream != null && outputStream != null;
        System.out.println("Connected to " + getTarget().getHostAddress() + " at port " + connectionPort);
    }

    private static void clientConn(){
        try {
            Socket client_socket = new Socket(getTarget(), connectionPort);
                if (!isConnOpen.compareAndSet(false, true)) {
                    client_socket.close();
                    return;
                }

                System.out.println("Opened client");
                socket = client_socket;
                inputStream = new MultipleFormatInputStream(socket.getInputStream());
                outputStream = new MultipleFormatOutputStream(socket.getOutputStream());
        } catch (ConnectException c){
            if (!isConnOpen.get()) c.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serverConn(){
        try {
            serverSocket = new ServerSocket(connectionPort);
            serverSocket.setSoTimeout(500);
            Socket conn_socket = null;
            while (conn_socket == null){
                try {
                    conn_socket = serverSocket.accept();
                } catch (SocketTimeoutException s) {
                    if (isConnOpen.get()) {
                        serverSocket.close();
                        serverSocket = null;
                        return;
                    }
                }
            }
            if (!isConnOpen.compareAndSet(false, true)) {
                conn_socket.close();
                serverSocket.close();
                serverSocket = null;
                return;
            }
            System.out.println("Opened server");
            socket = conn_socket;
            inputStream = new MultipleFormatInputStream(socket.getInputStream());
            outputStream = new MultipleFormatOutputStream(socket.getOutputStream());
        } catch (SocketException b) {
            if (!isConnOpen.get()) b.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void DataTransferExecute(){
        Thread input = new Thread(TransferConnector::processInput);
        Thread output = new Thread(TransferConnector::processOutput);
        input.start();
        output.start();
        try {
            input.join();
            output.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void processInput(){
        String s;
        while (true){
            s = (String)inputStream.readNext(null, false)[1];
            System.out.println("Remote Clipboard New: " + s);
            ClipboardIO.setSysClipboardText(s);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    static void processOutput(){
        int hash = ClipboardIO.getLastHash();
        while (true){
            try {
                ClipboardIO.checknew();
                if (hash != ClipboardIO.getLastHash()) {
                    hash = ClipboardIO.getLastHash();
                    if (!ClipboardIO.isLastFromRemote())
                        outputStream.writeString((String) ClipboardIO.getLast());
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    static void close(){
        try {
            if (socket != null)
                socket.close();
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
