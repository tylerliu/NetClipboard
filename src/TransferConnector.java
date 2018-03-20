import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TylerLiu on 2017/08/28.
 */
public class TransferConnector{

    private static final boolean isLoopBack = false;
    private static final int connectionPort = 31415;
    static MultipleFormatInBuffer inBuffer;
    static MultipleFormatOutBuffer outBuffer;
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
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(getTarget(), connectionPort));

            Selector selector = Selector.open();
            SelectionKey server_key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SelectionKey client_key = socketChannel.register(selector, SelectionKey.OP_CONNECT);

            wait_loop: while (true) {
                selector.select();
                for (SelectionKey s : selector.selectedKeys()) {
                    if (s == client_key) {
                        System.out.println("Opened client");
                        serverSocketChannel.close();
                        serverSocketChannel = null;
                        break wait_loop;
                    }
                    if (s == server_key) {
                        System.out.println("Opened server");
                        socketChannel.close();
                        socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        break wait_loop;
                    }

                }
            }

            inBuffer = new MultipleFormatInBuffer(socketChannel);
            outBuffer = new MultipleFormatOutBuffer(socketChannel);
            selector.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static void DataTransferExecute(){
        try {
            Selector selector = Selector.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);

            while (true) {
                if (selector.select(50) != 0)
                    for (SelectionKey s1 : selector.selectedKeys()) {
                        if (s1.isReadable()) {
                            Object[] b = inBuffer.readNext(null, false);
                            if (b == null) System.exit(0);
                            switch (ClipboardIO.getContentType((int) b[0])) {
                                case STRING:
                                    String s = (String) b[1];
                                    System.out.println("Remote Clipboard New: " + s);
                                    ClipboardIO.setSysClipboardText(s);
                                    break;
                                case HTML:
                                case FILES:
                                default:
                            }
                            return;
                        }
                        if (s1.isWritable()) {
                            System.out.println("Opened client");
                        }
                    }

                //writing
                try {
                    if (ClipboardIO.checknew() && !ClipboardIO.isLastFromRemote()){
                        switch (ClipboardIO.getLastType()){
                            case STRING:
                                outBuffer.writeString((String) ClipboardIO.getLast());
                                break;
                            case HTML:
                            case FILES:
                                break;
                            case END:
                                System.exit(0);
                            default:
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }


    static void close(){
        try {
            if (socketChannel != null)
                //socketChannel.configureBlocking(true);
                socketChannel.write(ByteBuffer.wrap(new byte[]{4, 0, 0, 0}));
                socketChannel.close();
            if (serverSocketChannel != null)
                serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
