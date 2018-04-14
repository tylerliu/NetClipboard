package net;

import files.Cancelable;
import files.FileReceiver;
import files.FileSender;
import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for receiving files
 */
public class FileTransfer {

    private static ConcurrentLinkedDeque<File> tempFolders = new ConcurrentLinkedDeque<>();
    private static boolean isCancelled;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Cancelable transferConnector;
    private static boolean isReceiveScheduled;
    private static boolean isFinished;

    public synchronized static CompletableFuture<List<File>> receiveFiles(ByteBuffer spec) {
        if (isTransferring()) cancelTransfer();
        isReceiveScheduled = true;
        isCancelled = false;
        isFinished = false;
        return CompletableFuture.supplyAsync(() -> receiveFilesWorker(spec), executor);
    }

    private static List<File> receiveFilesWorker(ByteBuffer spec) {
        List<File> files;
        int port = Short.toUnsignedInt(spec.getShort());
        byte[] master = new byte[spec.remaining()];
        spec.get(master);
        Cipher cipher = getCipher(master, false);
        try {

            File newDstFolder = Files.createTempDirectory("NetClipboard").toFile();
            newDstFolder.deleteOnExit();
            System.out.println("Receive Folder: " + newDstFolder.getAbsolutePath());

            System.out.println("File receiving from port: " + port);
            FileReceiver receiver = FileReceiver.receiveTarObj(port);
            transferConnector = receiver;
            files = receiver.runTared(newDstFolder, cipher);
            transferConnector = null;

            if (isCancelled) {
                FileUtils.deleteQuietly(newDstFolder);
                return null;
            }

            isFinished = true;

            deleteFolder();
            tempFolders.add(newDstFolder);
            System.out.println("File receive done");
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void sendFiles(List<File> sendFiles, int port, byte[] key) {
        if (isTransferring()) cancelTransfer();
        isReceiveScheduled = true;
        isCancelled = false;
        isFinished = false;
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles, port, key));
    }

    public static void sendFilesWorker(List<File> files, int port, byte[] key) {
        Cipher cipher = getCipher(key, true);
        System.out.println("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        transferConnector = sender;
        sender.runTared(files, cipher);
        isFinished = true;

    }

    public synchronized static void cancelTransfer() {
        if (isCancelled) return;
        isCancelled = true;
        if (isTransferring() && transferConnector != null) {
            transferConnector.cancel();
        }
        System.out.println("File receive cancelled");
    }

    public synchronized static boolean isTransferring() {
        return !isFinished && isReceiveScheduled;
    }

    public static Cipher getCipher(byte[] master, boolean isEncrypt) {
        try {
            byte[] keyBytes = Arrays.copyOfRange(master, 0, 1 << 4);
            byte[] nonce = Arrays.copyOfRange(master, 1 << 4, 1 << 5);
            byte[] aad = Arrays.copyOfRange(master, 1 << 5, 3 << 4);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(16 * 8, nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            if (isEncrypt) cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            else cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            cipher.updateAAD(aad);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void terminate() {
        if (isTransferring()) cancelTransfer();
        executor.shutdown();
    }

    public synchronized static void deleteFolder() {
        while (!tempFolders.isEmpty()) {
            if (tempFolders.peek() == null || !tempFolders.peek().exists() ||
                    FileUtils.deleteQuietly(tempFolders.peek())) tempFolders.pop();
        }
    }
}
