import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * Handles the network connection for clipboard sharing
 */
class TransferConnector {

    private static final boolean isLoopBack = false;
    private static final int connectionPort = 31415;
    private static MultipleFormatInBuffer inBuffer;
    private static MultipleFormatOutBuffer outBuffer;
    private static SocketChannel socketChannel;
    private static ServerSocketChannel serverSocketChannel;
    private static SocketChannel serverChannel;
    private static boolean terminateInitiated;

    private static InetAddress getTarget() {
        if (isLoopBack) return InetAddress.getLoopbackAddress();
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


    static void connect() {
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

            conn_loop:
            while (true) {
                selector.select();

                for (SelectionKey s : selector.selectedKeys()) {
                    if (s == client_key) {
                        System.out.println("Client Connected");
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();
                        }
                        s.cancel();
                    }
                    if (s == server_key) {
                        System.out.println("Opened server");
                        serverChannel = serverSocketChannel.accept();
                        serverChannel.configureBlocking(false);
                        s.cancel();
                    }
                    if (selector.keys().size() == 1) break conn_loop;
                }
            }

            inBuffer = new MultipleFormatInBuffer();
            outBuffer = new MultipleFormatOutBuffer();
            selector.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static void DataTransferExecute() {
        try {
            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_READ);
            socketChannel.register(selector, SelectionKey.OP_WRITE);

            while (true) {
                selector.select();
                for (SelectionKey s1 : selector.selectedKeys()) {
                    if (s1.isReadable()) {
                        //read

                        serverChannel.read(inBuffer.getInput().peekLast());
                        while (!inBuffer.getInput().peekLast().hasRemaining()) {
                            inBuffer.requestNext();
                            serverChannel.read(inBuffer.getInput().peekLast());
                        }

                        //set clipboard
                        if (inBuffer.readyToRead()) {
                            Object[] b = inBuffer.readNext();
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
                                default:
                            }
                        }
                    }
                    if (s1.isWritable()) {

                        //check clipboard
                        if (ClipboardIO.checkNew() && !ClipboardIO.isLastFromRemote()) {
                            switch (ClipboardIO.getLastType()) {
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

                        //send

                        while (!outBuffer.getOutput().isEmpty()) {
                            socketChannel.write(outBuffer.getOutput().peek());
                            if (outBuffer.getOutput().peek().hasRemaining()) break;
                            outBuffer.getOutput().poll();
                        }
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    //do nothing
                }
            }

        } catch (IOException e) {
            //do nothing
        }
    }

    static void close() {
        try {
            if (!terminateInitiated) {
                terminateInitiated = true;
                outBuffer.writeEND();
                while (!outBuffer.getOutput().isEmpty()) {
                    socketChannel.write(outBuffer.getOutput().peek());
                    if (!outBuffer.getOutput().peek().hasRemaining()) outBuffer.getOutput().poll();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
            if (socketChannel != null)
                socketChannel.close();
            if (serverChannel != null)
                serverChannel.close();
            if (serverSocketChannel != null)
                serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
