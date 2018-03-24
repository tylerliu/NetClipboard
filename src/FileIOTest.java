import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by TylerLiu on 2018/03/23.
 */
public class FileIOTest {

    public static void main(String[] args) {
        //send();
        //receive();
    }

    public static void receive() {
        File dst = new File("src.zip");
        try {
            dst.createNewFile();
            FileReceiver.receiveFile(dst).join();
            Decompressor.decompress("src.zip", "src_2");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void send() {
        List<File> files = new ArrayList<>();
        files.add(new File("src"));
        FileSender.sendFileList(files);
        FileSender.terminate();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}