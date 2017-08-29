import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown: closing ports");
                TransferConnector.close();
            }));
	// write your code here
        TransferConnector.connect();
        try {
            TransferConnector.outputStream.writeUTF(TransferConnector.localHost.toString());
            System.out.println(TransferConnector.inputStream.readUTF());

            //clipboard
            String s = ClipboardIO.getSysClipboardText();
            TransferConnector.outputStream.writeUTF(s);
            System.out.println(TransferConnector.inputStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TransferConnector.close();
        System.exit(0);
    }
}
