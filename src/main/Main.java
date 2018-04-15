package main;

import clip.ClipboardIO;
import key.KeyUtil;
import net.FileTransfer;
import net.FileTransferMode;
import net.TransferConnector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        /**
         * arguments that will not make connection
         */
        if (args.length >= 1) {
            if (args[0].toLowerCase().startsWith("-h") || args[0].toLowerCase().equals("--help")) {
                printHelp();
                return;
            }
            if (args[0].toLowerCase().startsWith("-g")) {
                KeyUtil.generateKey();
                return;
            }
        }

        if (!KeyUtil.isKeyExist()) {
            System.out.println("Encryption key file not found. Please generate encryption key with -g option.");
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown: closing ports");
            TransferConnector.close();
            FileTransfer.terminate();
        }));

        try {
            System.out.println("This computer is " + InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        if (args.length >= 1 && args[0].toLowerCase().startsWith("-c")) {
            FileTransferMode.setLocalMode(FileTransferMode.Mode.CACHED);
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        /**
         * target options
         */
        if (args.length >= 1) {
            if (args[0].toLowerCase().startsWith("-m")) {
                TransferConnector.setManualTarget();
            } else if (args.length >= 2 && args[0].toLowerCase().startsWith("-d")) {
                TransferConnector.setDirectTarget(args[1]);
            } else System.out.println("Unknown Command. Use \"-h\" to show more options");
        }

        TransferConnector.setTarget();
        ClipboardIO.getSysClipboardText();
        if (!TransferConnector.connect()) return;
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }

    public static void printHelp() {
        System.out.println("Net Clipboard");
        System.out.println("Shared Clipboard between computers");
        System.out.println("Options:");
        System.out.println("\t\t\t-c\tAllow Pasting by Clipboard (Windows only)");
        System.out.println("\t-h, --help\tShow Help");
        System.out.println("\t\t\t-g\tGenerate Encryption Key File In Current Directory");
        System.out.println("\t\t\t-m\tManually select the other computer to share clipboard");
        System.out.println("\t\t\t-d\tSpecify the other computer to share clipboard");
    }
}
