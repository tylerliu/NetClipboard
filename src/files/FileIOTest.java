package files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A test for file receiver and sender
 */
public class FileIOTest {

    public static void main(String[] args) {
        //send();
        //receive();
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
        files.add(new File("src"));
        try {
            FileSender.sendFileList(files).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
