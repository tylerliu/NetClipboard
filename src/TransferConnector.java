import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TylerLiu on 2017/08/28.
 */
public class TransferConnector{

    static final boolean isLoopBack = false;
    static final int connectionPort = 31415;
    static MultipleFormatInputStream inputStream;
    static MultipleFormatOutputStream outputStream;
    static SocketChannel socketChannel;
    static ServerSocketChannel serverSocketChannel;
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
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(connectionPort));
            serverSocketChannel.configureBlocking(false);
            socketChannel = SocketChannel.open(new InetSocketAddress(getTarget(), connectionPort));
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            SelectionKey server_key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SelectionKey client_key = socketChannel.register(selector, SelectionKey.OP_CONNECT);

            while (true) {
                selector.select();
                for (SelectionKey s : selector.selectedKeys()) {
                    if (s == server_key) {
                        System.out.println("Opened server");
                        socketChannel = serverSocketChannel.accept();
                        return;
                    }
                    if (s == client_key) {
                        System.out.println("Opened client");
                        serverSocketChannel.close();
                        serverSocketChannel = null;
                        return;
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
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
            Object[] b = inputStream.readNext(null, false);
            if (b == null) System.exit(0);
            switch (ClipboardIO.getContentType((Integer)b[0])){
                case STRING:
                    s = (String) b[1];
                    System.out.println("Remote Clipboard New: " + s);
                    ClipboardIO.setSysClipboardText(s);
                    break;
                case HTML:
                    break;
                case FILES:
                default:
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    static void processOutput(){
        while (true){
            try {
                if (ClipboardIO.checknew() && !ClipboardIO.isLastFromRemote()){
                    switch (ClipboardIO.getLastType()){
                        case STRING:
                            outputStream.writeString((String) ClipboardIO.getLast());
                            break;
                        case HTML:
                            break;
                        case FILES:
                        default:
                    }
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
            if (socketChannel != null)
                socketChannel.close();
            if (serverSocketChannel != null)
                serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
