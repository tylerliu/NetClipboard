package main;

import net.FileTransfer;
import net.TransferConnector;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        if (args.length >= 1 && args[0].toLowerCase().startsWith("-k")) {
            keygen.Keygen.keyToFile(new File("./.NetClipboardKey"));
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown: closing ports");
            TransferConnector.close();
            FileTransfer.terminate();
        }));
        ClipboardIO.getSysClipboardText();
        if (!TransferConnector.connect()) return;
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }
}