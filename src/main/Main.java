package main;

import clip.ClipboardIO;
import key.KeyUtil;
import net.FileTransfer;
import net.FileTransferMode;
import net.TransferConnector;
import org.apache.commons.cli.*;
import ui.UserInterfacing;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {

        System.setProperty("apple.awt.UIElement", "true");
        UserInterfacing.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            UserInterfacing.setConnStatus("Shutdown: closing ports");
            TransferConnector.close();
            FileTransfer.terminate();
        }));

        parseCommand(args);
        if (!KeyUtil.isKeyExist()) {
            UserInterfacing.setKey(false);
            System.exit(0);
        }

        try {
            UserInterfacing.printInfo("This computer is " + InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            UserInterfacing.printError(e);
        }
        TransferConnector.setTarget();
        ClipboardIO.checkNew();
        if (!TransferConnector.connect()) return;
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }

    public static void parseCommand(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(getOptions(), args);
        } catch (ParseException e) {
            UserInterfacing.printInfo(e.getLocalizedMessage());
            printHelp();
            //UserInterfacing.printError(e);
            System.exit(0);
            return;
        }
        if (UserInterfacing.isCommandLine()) {
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            if (cmd.hasOption('g')) {
                KeyUtil.generateKey();
                System.exit(0);
            }
        }

        if (cmd.hasOption('c')) FileTransferMode.setLocalMode(FileTransferMode.Mode.CACHED);
        if (cmd.hasOption('r')) TransferConnector.setDirectTarget(cmd.getOptionValue('r'));
        if (cmd.hasOption('m')) TransferConnector.setManualTarget();
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
