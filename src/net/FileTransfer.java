package net;

import filechooser.NativeJFileChooser;
import files.FileReceiver;
import files.FileSender;
import org.apache.commons.io.FileUtils;
import tray.Interfacing;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFileChooser;
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
            System.out.println("Retrieve files to: " + toDir.getAbsolutePath());
        } else {
            System.out.println("Cancelled Pasting.");
            FileReceiver.cancelConnection(port);
            return null;
        }

        System.out.println("File receiving from port: " + port);
        FileReceiver receiver = FileReceiver.receiveTarObj(port);
        receivers.add(receiver);
        files = receiver.runTared(toDir, cipher);
        receivers.remove(receiver);

        if (receiver.isCancelled()) {
            deleteTempFolder();
            return null;
        }

        deleteTempFolder(toDir);
        System.out.println("File receive done");
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
                Interfacing.printError(e);
                return null;
            }
        } else {
            //choose destination
            //TODO track default directory
            if (lastSavedDirectory == null) {
                lastSavedDirectory = new File(System.getProperty("user.home"));
            }
            NativeJFileChooser chooser = new NativeJFileChooser(lastSavedDirectory);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Paste Files...");

            int chooseResult = chooser.showDialog(null, "Paste");
            if (chooseResult == JFileChooser.APPROVE_OPTION) {
                return lastSavedDirectory = chooser.getSelectedFile();
            } else {
                return null;
            }
        }
    }

    public synchronized static void sendFiles(List<File> sendFiles, int port, byte[] key) {
        if (FileTransferMode.getTargetMode() == FileTransferMode.Mode.CACHED) cancelSend();
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles, port, key));
    }

    public static void sendFilesWorker(List<File> sendFiles, int port, byte[] key) {
        Cipher cipher = getCipher(key, true);
        System.out.println("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        senders.add(sender);
        sender.runTared(sendFiles, cipher);
        senders.remove(sender);
    }

    public synchronized static void cancelReceive() {
        if (!receivers.isEmpty()) System.out.println("File receive cancelled");
        for (FileReceiver receiver : receivers) {
            receiver.cancel();
        }
    }

    public synchronized static void cancelSend() {
        if (!senders.isEmpty()) System.out.println("File send cancelled");
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
            Interfacing.printError(e);
        }
        return null;
    }

    public synchronized static void terminate() {
        attemptCancelTransfer();
        executor.shutdown();
        while (!executor.isTerminated()) {
            System.out.println("Wait for transferring...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Interfacing.printError(e);
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
