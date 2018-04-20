package main;

import clip.ClipboardIO;
import key.KeyUtil;
import net.FileTransfer;
import net.FileTransferMode;
import net.TransferConnector;
import org.apache.commons.cli.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(getOptions(), args);
        } catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
            printHelp();
            //e.printStackTrace();
            return;
        }

        /**
         * arguments that will not make connection
         */
        if (cmd.hasOption('h')) {
            printHelp();
            return;
        }
        if (cmd.hasOption('g')) {
            KeyUtil.generateKey();
            return;
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


        if (cmd.hasOption('c')) {
            FileTransferMode.setLocalMode(FileTransferMode.Mode.CACHED);
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        if (cmd.hasOption('m')) {
            TransferConnector.setManualTarget();
        }
        else if (cmd.hasOption('r')) {
            TransferConnector.setDirectTarget(cmd.getOptionValue('r'));
        }


        TransferConnector.setTarget();
        ClipboardIO.checkNew();
        if (!TransferConnector.connect()) return;
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }

    public static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder("c")
                .longOpt("cached")
                .desc("Allow Pasting by Clipboard (Windows only)")
                .build());
        options.addOption(Option.builder("g")
                .longOpt("generate")
                .desc("Generate Encryption Key File In Current Directory")
                .build());
        options.addOption(Option.builder("m")
                .longOpt("manual")
                .desc("Select the other computer to share clipboard Manually")
                .build());
        options.addOption(Option.builder("r")
                .longOpt("remote")
                .desc("Specify the other computer to share clipboard with")
                .hasArg()
                .argName("REMOTE")
                .build());
        options.addOption(Option.builder("h").longOpt("help").desc("Show Help").build());
        return options;
    }

    public static void printHelp() {
        String header = "Shared Clipboard between computers";
        String footer = "";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("netClipboard", header, getOptions(), footer, true);
    }
}
