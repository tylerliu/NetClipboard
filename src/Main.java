
public class Main {

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown: closing ports");
            TransferConnector.close();
            FileTransfer.terminate();
        }));
        ClipboardIO.getSysClipboardText();
        TransferConnector.connect();
        TransferConnector.DataTransferExecute();
        TransferConnector.close();
        System.exit(0);
    }
}
