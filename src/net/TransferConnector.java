package net;

import clip.ClipboardIO;
import format.DataFormat;
import format.FormattedInStream;
import format.FormattedOutStream;
import net.handshake.*;
import org.bouncycastle.crypto.tls.TlsProtocol;
import tray.Interfacing;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the network connection for clipboard sharing
 */
public class TransferConnector {

    private static final int connectionPort = 31415;
    private static boolean isServer;
    private static FormattedInStream inStream;
    private static FormattedOutStream outStream;
    private static Socket socket;
    private static ServerSocket serverSocket;
    private static boolean terminateInitiated;
    private static TlsProtocol tlsProtocol;
    private static InetAddress target;

    /**
     * set target by Key Based Handshake
     */
    public static void setTarget() {
        if (target != null) return;
        System.out.println("Connecting...");
        target = KeyBased.getTarget();
    }

    public static void setDirectTarget(String name) {
        if (target != null) return;
        target = DirectConnect.getTarget(name);
    }

    public static void setManualTarget() {
        if (target != null) return;
        target = Manual.getTarget();
    }

    public static InetAddress getTarget() {
        return target;
    }

    public static void resetTarget() {
        target = null;
    }

    /**
     * determine if this is the server side of the connection
     */
    private static void checkServer() {
        try {
            isServer = Arrays.compare(InetAddress.getLocalHost().getAddress(), getTarget().getAddress()) > 0;
        } catch (UnknownHostException e) {
            Interfacing.printError(e);
        }
    }


    public static boolean connect() {
        try {
            checkServer();
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

            tlsProtocol = TLSHandler.getTlsProtocol(isServer, socket.getInputStream(), socket.getOutputStream());
            if (tlsProtocol == null) return false;
            exchangeProtocol(tlsProtocol);
            inStream = new FormattedInStream(tlsProtocol.getInputStream());
            outStream = new FormattedOutStream(tlsProtocol.getOutputStream());

        } catch (IOException e) {
            Interfacing.printError(e);
            System.exit(0);
        }

        return true;
    }

    private static void exchangeProtocol(TlsProtocol protocol) {
        try {
            int magicNumber = 5;
            protocol.getOutputStream().write(magicNumber);
            protocol.getOutputStream().write(FileTransferMode.getLocalMode().ordinal());
            if (protocol.getInputStream().read() != magicNumber) {
                System.out.println("Communication protocol does not match! ");
                System.exit(1);
            }
            int targetMode = protocol.getInputStream().read();
            if (targetMode != -1) {
                FileTransferMode.setTargetMode(FileTransferMode.Mode.values()[targetMode]);
            }
        } catch (Exception e) {
            Interfacing.printError(e);
            System.out.println("protocol exchange failed");
            System.exit(1);
        }
    }

    public static void DataTransferExecute() {
        Thread thread = new Thread(TransferConnector::writer, "writer");
        thread.start();
        //read thread
        reader();
    }

    private static void writer() {
        try {
            while (true) {

                //check clipboard
                if (ClipboardIO.checkNew()) {
                    FileTransfer.attemptCancelTransfer();//if file Transferring
                    FileTransfer.deleteTempFolder();
                    switch (ClipboardIO.getLastType()) {
                        case DataFormat.STRING:
                            outStream.writeSTRING(ClipboardIO.getLastString());
                            break;
                        case DataFormat.HTML:
                        case DataFormat.FILES:
                            int port = PortAllocator.alloc();
                            byte[] key = getTransKey();
                            outStream.writeFiles(port, key);
                            FileTransfer.sendFiles(ClipboardIO.getLastFiles(), port, key);
                            break;
                        case DataFormat.END_SIGNAL:
                            return;
                        default:
                    }
                }

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }

        } catch (IOException e) {
            //do nothing
            if (!terminateInitiated) Interfacing.printError(e);
        }
    }

    private static byte[] getTransKey() {
        byte[] key = new byte[48];
        try {
            SecureRandom.getInstanceStrong().nextBytes(key);
        } catch (NoSuchAlgorithmException e) {
            Interfacing.printError(e);
        }
        return key;
    }

    private static void reader() {
        //read
        try {
            while (true) {
                int type = inStream.nextEntry();
                FileTransfer.attemptCancelTransfer(); //cancel if file transferring
                FileTransfer.deleteTempFolder();
                switch (type) {
                    case DataFormat.STRING:
                        String s = inStream.getString();
                        System.out.println("Remote Clipboard New: " + s);
                        ClipboardIO.setSysClipboardText(s);
                        break;
                    case DataFormat.END_SIGNAL:
                        terminateInitiated = true;
                        return;
                    case DataFormat.FILES:
                        CompletableFuture<List<File>> re = FileTransfer.receiveFiles(inStream.getFiles());
                        if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED) {
                            re.thenAccept((files) -> {
                                if (files == null) return;
                                System.out.println("Remote Clipboard New: " + files);
                                ClipboardIO.setSysClipboardFiles(files);
                            });
                        } else {
                            ClipboardIO.unsetSysClipboard();
                        }
                    default:
                }
            }
        } catch (IOException e) {

            if (!terminateInitiated) Interfacing.printError(e);
        }
    }

    public static void close() {
        try {
            if (!terminateInitiated && socket != null && outStream != null && socket.isConnected()) {
                terminateInitiated = true;
                outStream.writeEND();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
            if (tlsProtocol != null)
                tlsProtocol.close();
            if (socket != null)
                socket.close();
            if (isServer && serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            Interfacing.printError(e);
            e.printStackTrace();
        }
    }
}
