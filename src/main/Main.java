package main;

import net.FileTransfer;
import net.TransferConnector;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        if (args.length >= 1) {

            if (args[0].toLowerCase().startsWith("-h") || args[0].toLowerCase().equals("--help")) {
                System.out.println("Net Clipboard");
                System.out.println("Shared Clipboard between computers");
                System.out.println("Options:");
                System.out.println("\t-h, --help\tShow Help");
                System.out.println("\t\t\t-k\tGenerate Key File In Current Directory");
                System.out.println("\t\t\t-m\tManually select the other computer to share clipboard");
                System.out.println("\t\t\t-d\tSpecify the other computer to share clipboard");
                return;
            }
            else if (args[0].toLowerCase().startsWith("-k")) {
                keygen.Keygen.keyToFile(new File("./.NetClipboardKey"));
                return;
            }
            else if (args[0].toLowerCase().startsWith("-m")) {
                TransferConnector.setManualTarget();
                return;
            }
            else if (args.length >= 2 && args[0].toLowerCase().startsWith("-d")){
                TransferConnector.setDirectTarget(args[1]);
            }
            else System.out.println("Unknown Command. Use \"-h\" to show more options");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown: closing ports");
            TransferConnector.close();
            FileTransfer.terminate();
        }));
        TransferConnector.setTarget();
        ClipboardIO.getSysClipboardText();
        if (!TransferConnector.connect()) return;
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }
}
