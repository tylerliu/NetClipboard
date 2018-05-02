package net;

import files.FileReceiver;
import files.FileSender;
import org.apache.commons.io.FileUtils;
import ui.UserInterfacing;

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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for transferring files
 */
public class FileTransfer {

    private static ConcurrentLinkedDeque<File> tempFolders = new ConcurrentLinkedDeque<>();
    private static File lastSavedDirectory;
    private static ExecutorService executor = Executors.newWorkStealingPool();
    private static List<FileReceiver> receivers = Collections.synchronizedList(new LinkedList<>());
    private static List<FileSender> senders = Collections.synchronizedList(new LinkedList<>());

    public synchronized static CompletableFuture<List<File>> receiveFiles(ByteBuffer spec) {
        if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED) cancelReceive(); //cancel other receive
        return CompletableFuture.supplyAsync(() -> receiveFilesWorker(spec), executor);
    }

    private static List<File> receiveFilesWorker(ByteBuffer spec) {
        List<File> files;
        int port = Short.toUnsignedInt(spec.getShort());
        byte[] master = new byte[spec.remaining()];
        spec.get(master);
        Cipher cipher = getCipher(master, false);

        File toDir = getSavingDirectory();
        if (toDir != null) {
            UserInterfacing.printInfo("Retrieve files to: " + toDir.getAbsolutePath());
        } else {
            UserInterfacing.setClipStatus("Cancelled File Pasting");
            FileReceiver.cancelConnection(port);
            return null;
        }

        UserInterfacing.printInfo("File receiving from port: " + port);
        FileReceiver receiver = FileReceiver.receiveTarObj(port);
        if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED)receivers.add(receiver);
        files = receiver.runTared(toDir, cipher);
        receivers.remove(receiver);

        if (receiver.isCancelled()) {
            deleteTempFolder();
            return null;
        }

        deleteTempFolder(toDir);
        UserInterfacing.setClipStatus("File received");
        return files;
    }

    private static File getSavingDirectory() {
        if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED) {
            try {
                File newDstFolder = Files.createTempDirectory("NetClipboard").toFile();
                newDstFolder.deleteOnExit();
                tempFolders.add(newDstFolder);
                return newDstFolder;
            } catch (IOException e) {
                UserInterfacing.printError(e);
                return null;
            }
        } else {
            return UserInterfacing.getSaveDir();
        }
    }

    public synchronized static void sendFiles(List<File> sendFiles, int port, byte[] key) {
        if (FileTransferMode.getTargetMode() == FileTransferMode.Mode.CACHED) cancelSend();
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles, port, key));
    }

    public static void sendFilesWorker(List<File> sendFiles, int port, byte[] key) {
        Cipher cipher = getCipher(key, true);
        UserInterfacing.printInfo("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        if (FileTransferMode.getTargetMode() == FileTransferMode.Mode.CACHED) senders.add(sender);
        sender.runTared(sendFiles, cipher);
        senders.remove(sender);
        UserInterfacing.setClipStatus("File Sent");
    }

    public synchronized static void cancelReceive() {
        if (!receivers.isEmpty()) UserInterfacing.setClipStatus("Cancelled File Pasting");
        for (FileReceiver receiver : receivers) {
            receiver.cancel();
        }
    }

    public synchronized static void cancelSend() {
        if (!senders.isEmpty()) UserInterfacing.setClipStatus("Cancelled File Sending");
        for (FileSender sender : senders) {
            sender.cancel();
        }
    }

    public synchronized static void attemptCancelTransfer() {
        if (FileTransferMode.getLocalMode() == FileTransferMode.Mode.CACHED) cancelReceive();
        if (FileTransferMode.getTargetMode() == FileTransferMode.Mode.CACHED) cancelSend();
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
            UserInterfacing.printError(e);
        }
        return null;
    }

    public synchronized static void terminate() {
        attemptCancelTransfer();
        executor.shutdown();
        while (!executor.isTerminated()) {
            UserInterfacing.setConnStatus("Wait for transferring...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UserInterfacing.printError(e);
            }
        }
    }

    public synchronized static void deleteTempFolder() {
        while (!tempFolders.isEmpty()) {
            if (tempFolders.peek() == null || !tempFolders.peek().exists() ||
                    FileUtils.deleteQuietly(tempFolders.peek())) {
                tempFolders.poll();
            } else {
                return;
            }
        }
    }

    public synchronized static void deleteTempFolder(File exclude) {
        while (!tempFolders.isEmpty()) {
            if (tempFolders.peek() == null || !tempFolders.peek().exists() ||
                    (!tempFolders.peek().equals(exclude) && FileUtils.deleteQuietly(tempFolders.peek()))) {
                tempFolders.poll();
            } else {
                return;
            }
        }
    }
}
