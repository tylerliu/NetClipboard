package files;

import net.TransferConnector;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A test for file receiver and sender
 */
public class FileIOTest {

    public static void main(String[] args) {
        try {
            TransferConnector.setDirectTarget(InetAddress.getLoopbackAddress().getHostName());
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            executorService.submit(FileIOTest::send);
            Thread.sleep(50);
            executorService.submit(FileIOTest::receive);
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void receive() {
        try {
            FileReceiver.receiveTar(new File("src_2")).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void send() {
        List<File> files = new ArrayList<>();
        files.add(new File("src/key"));
        try {
            FileSender.sendFileList(files).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
