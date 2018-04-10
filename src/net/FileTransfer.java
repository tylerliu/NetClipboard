package net;

import Keygen.Keygen;
import filechooser.NativeJFileChooser;
import files.FileReceiver;
import files.FileSender;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for transferring files
 */
public class FileTransfer {

    private static File lastSavedDirectory;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    public synchronized static void receiveFiles(ByteBuffer spec) {
        executor.submit(() -> receiveFilesWorker(spec));
    }

    private static void receiveFilesWorker(ByteBuffer spec) {
        int port = Short.toUnsignedInt(spec.getShort());
        byte[] master = new byte[spec.remaining()];
        spec.get(master);
        Cipher cipher = getCipher(master, false);
        File toDir;

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
            toDir = chooser.getSelectedFile();
            lastSavedDirectory = toDir;
            System.out.println("Saving to: " + lastSavedDirectory.getAbsolutePath());
        } else {
            System.out.println("Cancelled Pasting.");
            FileReceiver.cancelConnection(port);
            return;
        }

        System.out.println("File receiving from port: " + port);
        FileReceiver receiver = FileReceiver.receiveTarObj(port);
        receiver.runTared(toDir, cipher);
        System.out.println("File receive done");
    }

    public synchronized static void sendFiles(List<File> sendFiles, int port, byte[] key) {
        executor.submit(() -> FileTransfer.sendFilesWorker(sendFiles, port, key));
    }

    public static void sendFilesWorker(List<File> sendFiles, int port, byte[] key) {
        Cipher cipher = getCipher(key, true);
        System.out.println("File sending on port: " + port);
        FileSender sender = FileSender.sendFileListObj(port);
        sender.runTared(sendFiles, cipher);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void terminate() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            System.out.println("Wait for transferring...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
