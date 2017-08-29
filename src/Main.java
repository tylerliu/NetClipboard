import javax.sound.sampled.Clip;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
	// write your code here
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        TransferConnector.connect();
        try {
            TransferConnector.outputStream.writeUTF(TransferConnector.localHost.toString());
            System.out.println(TransferConnector.inputStream.readUTF());

            //clipboard

            while (ClipboardIO.queue.isEmpty()) ClipboardIO.checknew();
            String s = ClipboardIO.queue.pop();
            TransferConnector.outputStream.writeUTF(s);
            System.out.println(TransferConnector.inputStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TransferConnector.close();
        System.exit(0);
    }
}
