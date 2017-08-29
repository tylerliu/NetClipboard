import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Created by TylerLiu on 2017/08/28.
 */
public class TransferConnector{

    static final boolean isLoopBack = true;
    static final int connectionPort = 31415;
    static DataInputStream inputStream;
    static DataOutputStream outputStream;
    static Socket socket;
    static ServerSocket serverSocket;
    static InetAddress localHost;

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
        try {
            socket = new Socket(getTarget(), connectionPort);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e) {
            try {
                System.out.println("client failed. trying server");
                serverSocket = new ServerSocket(connectionPort);
                socket = serverSocket.accept();
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException f) {
                f.printStackTrace();
            }
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
