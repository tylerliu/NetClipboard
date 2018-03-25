import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
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


    static void connect() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(connectionPort));
            serverSocketChannel.configureBlocking(false);
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            if (socketChannel.connect(new InetSocketAddress(getTarget(), connectionPort))) {
                System.out.println("Client Connected");
            } else {
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }

            while (true) {
                selector.select();

                for (SelectionKey s : selector.selectedKeys()) {
                    if (s.isConnectable()) {
                        System.out.println("Client Connected");
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();
                        }
                        s.cancel();
                        continue;
                    }
                    if (s.isAcceptable()) {
                        System.out.println("Opened server");
                        serverChannel = serverSocketChannel.accept();
                        serverChannel.configureBlocking(false);
                        s.cancel();
                    }
                }

                if (selector.keys().size() == 1) break;
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
                selector.select(25);

                //check clipboard
                if (ClipboardIO.checkNew()) {
                    if (FileTransfer.isTransfering()) { //file Transferring
                        FileTransfer.cancelTransfer();
                    }
                    switch (ClipboardIO.getLastType()) {
                        case STRING:
                            outBuffer.writeString(ClipboardIO.getLastString());
                            break;
                        case HTML:
                        case FILES:
                            //TODO run with random port?
                            outBuffer.writeFiles();
                            //TODO Fix another send file opened
                            FileSender.sendFileList(ClipboardIO.getLastFiles());
                            break;
                        case END:
                            return;
                        default:
                    }
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }

                //set clipboard if file receiving finished
                if (FileTransfer.isNewlyReceived()) {
                    List<File> files = FileTransfer.getFiles();
                    System.out.println("Remote Clipboard New: " + files);
                    ClipboardIO.setSysCLipboardFiles(files);
                }

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
                            if (FileTransfer.isTransfering()) {
                                FileTransfer.cancelTransfer();
                            }
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
                                default:
                            }
                        }

                    }

                    if (s1.isWritable()) {
                        //send
                        while (!outBuffer.getOutput().isEmpty()) {
                            socketChannel.write(outBuffer.getOutput().peek());
                            if (outBuffer.getOutput().peek().hasRemaining()) break;
                            outBuffer.getOutput().poll();
                        }
                        s1.cancel();
                    }
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
