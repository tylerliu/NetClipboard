package net;

import format.DataFormat;
import format.FormattedInStream;
import format.FormattedOutStream;
import javafx.scene.input.ClipboardContent;
import javafx.util.Pair;
import net.handshake.DirectConnect;
import net.handshake.KeyBased;
import net.handshake.Manual;
import org.bouncycastle.crypto.tls.TlsProtocol;
import ui.UserInterfacing;
import ui.clip.ClipboardIO;
import ui.clip.ContentUtil;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
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
        UserInterfacing.setConnStatus("Connecting...");
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
            UserInterfacing.printError(e);
        }
    }


    public static boolean connect() {
        try {
            if (target == null || target == InetAddress.getLocalHost()) return false;
            checkServer();
            UserInterfacing.printInfo("This is configured as " + (isServer ? "Server. " : "Client. "));
            if (isServer) {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(connectionPort));
                socket = serverSocket.accept();
                UserInterfacing.setConnStatus("Server Connected");
            } else {
                socket = new Socket(getTarget(), connectionPort);
                UserInterfacing.setConnStatus("Client Connected");
            }

            tlsProtocol = TLSHandler.getTlsProtocol(isServer, socket.getInputStream(), socket.getOutputStream());
            if (tlsProtocol == null) return false;
            exchangeProtocol(tlsProtocol);
            inStream = new FormattedInStream(tlsProtocol.getInputStream());
            outStream = new FormattedOutStream(tlsProtocol.getOutputStream());

        } catch (IOException e) {
            UserInterfacing.printError(e);
            System.exit(0);
        }

        return true;
    }

    private static void exchangeProtocol(TlsProtocol protocol) {
        try {
            int magicNumber = 7;
            protocol.getOutputStream().write(magicNumber);
            protocol.getOutputStream().write(FileTransferMode.getModeForSending().ordinal());
            if (protocol.getInputStream().read() != magicNumber) {
                UserInterfacing.printInfo("Communication protocol does not match! ");
            }
            int targetMode = protocol.getInputStream().read();
            if (targetMode != -1) {
                FileTransferMode.setTargetMode(FileTransferMode.Mode.values()[targetMode]);
            }
        } catch (Exception e) {
            UserInterfacing.printError(e);
            UserInterfacing.printInfo("protocol exchange failed");
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
                if (!FileTransferMode.getIsSent())
                    outStream.writeModeSet(FileTransferMode.getModeForSending());

                //check clipboard
                if (ClipboardIO.checkNew()) {
                    FileTransfer.attemptCancelTransfer();//if file Transferring
                    FileTransfer.deleteTempFolder();

                    //write content
                    ClipboardContent content = ClipboardIO.getLastContent();
                    if (content.keySet().size() > 1)
                        outStream.writeFormatCount((byte) content.keySet().size());
                    for (javafx.scene.input.DataFormat FXFormat : content.keySet()) {
                        if (content.get(FXFormat) instanceof String) {
                            outStream.writeGeneralString(FXFormat, (String) content.get(FXFormat));
                        }
                        if (content.get(FXFormat) instanceof ByteBuffer) {
                            outStream.writeByteBuffer(FXFormat, (ByteBuffer) content.get(FXFormat));
                        }
                    }

                    if (content.hasFiles()) {
                        int port = PortAllocator.alloc();
                        byte[] key = getTransKey();
                        outStream.writeFiles(port, key);
                        FileTransfer.sendFiles(content.getFiles(), port, key);
                    }
                    else if (content.hasImage()) {
                        if (content.hasUrl()) outStream.writeImageAsUrl();
                        else outStream.writeImage(content.getImage());
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
            if (!terminateInitiated) UserInterfacing.printError(e);
        }
    }

    private static byte[] getTransKey() {
        byte[] key = new byte[48];
        try {
            SecureRandom.getInstanceStrong().nextBytes(key);
        } catch (NoSuchAlgorithmException e) {
            UserInterfacing.printError(e);
        }
        return key;
    }

    private static void reader() {
        //read
        try {
            while (true) {
                ClipboardContent content = new ClipboardContent();
                int entryCount = 1;
                boolean asyncPush = false;
                int type = inStream.nextEntry();
                if (type == DataFormat.END_SIGNAL) {
                    terminateInitiated = true;
                    return;
                }
                if (type == DataFormat.MODE_SET) {
                    FileTransferMode.setTargetMode(inStream.getMode());
                    continue;
                }
                if (type == DataFormat.FORMAT_COUNT) {
                    entryCount = inStream.getFormatCount();
                }
                FileTransfer.attemptCancelTransfer(); //cancel if file transferring
                FileTransfer.deleteTempFolder();
                for (int i = 0; i < entryCount; i++) {
                    if (i != 0 || type == DataFormat.FORMAT_COUNT) type = inStream.nextEntry();
                    switch (type) {
                        case DataFormat.GENERAL_STRING:
                            Pair<javafx.scene.input.DataFormat, String> entry = inStream.getGeneralString();
                            content.put(entry.getKey(), entry.getValue());
                            break;
                        case DataFormat.BYTEBUFFER:
                            Pair<javafx.scene.input.DataFormat, ByteBuffer> bufferEntry = inStream.getByteBuffer();
                            content.put(bufferEntry.getKey(), bufferEntry.getValue());
                            break;
                        case DataFormat.FILES:
                            asyncPush = true;
                            CompletableFuture<List<File>> re = FileTransfer.receiveFiles(inStream.getFiles());
                            if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED) {
                                re.thenAccept((files) -> {
                                    if (files == null) return;
                                    content.putFiles(files);
                                    ContentUtil.printContent(content, "Remote");
                                    ClipboardIO.setSysClipboardContent(content);
                                });
                            } else {
                                ClipboardIO.unsetSysClipboard();
                            }
                            break;
                        case DataFormat.IMAGE:
                            content.putImage(inStream.getImage(content.getUrl()));
                            break;
                        default:
                            if (!terminateInitiated) UserInterfacing.printError(new IOException("Unacceptable format"));
                    }

                }
                if (!asyncPush && !terminateInitiated) {
                    ContentUtil.printContent(content, "Remote");
                    ClipboardIO.setSysClipboardContent(content);
                }
            }
        } catch (IOException e) {
            if (!terminateInitiated) UserInterfacing.printError(e);
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
            UserInterfacing.printError(e);
            e.printStackTrace();
        }
    }
}
