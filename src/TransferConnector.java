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
    static DataInputStream inputStream;
    static DataOutputStream outputStream;
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
        serverThread.start();
        clientThread.start();
        try {
            serverThread.join();
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("connected?");
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
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (ConnectException c){
            System.out.println(c.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serverConn(){
        try {
            serverSocket = new ServerSocket(connectionPort);
            Socket conn_socket = serverSocket.accept();
            if (!isConnOpen.compareAndSet(false, true)) {
                conn_socket.close();
                serverSocket.close();
                serverSocket = null;
                return;
            }
            System.out.println("Opened server");
            socket = conn_socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (BindException b) {
            System.out.println(b.getMessage());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void close(){
        try {
            socket.close();
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
