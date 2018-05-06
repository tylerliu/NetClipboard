package ui.clip;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import ui.UserInterfacing;

import java.util.Random;

public class ContentUtil {



    /* protected */ static boolean isContentEqual(ClipboardContent a, ClipboardContent b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.keySet().equals(b.keySet())) return false;

        //file comparison first
        if (a.hasFiles()) {
            return a.getFiles().equals(b.getFiles());
        }

        //String comparison
        for (DataFormat format : a.keySet()) {
            if (a.get(format) instanceof String && !a.get(format).equals(b.get(format))) return false;
        }

        if (!a.hasImage() && !b.hasImage()) return true;
        if (!a.hasImage() || !b.hasImage()) return false;
        if (a.hasUrl()) return true;
        else return compareImage(a.getImage(), b.getImage());
    }

    public static void printContent(ClipboardContent content, String attribute) {
        if (content.hasFiles()) {
            UserInterfacing.printInfo(attribute + " New: " + content.getFiles());
        }
        if (content.hasString()) {
            String s = content.getString().replaceAll("\r\n", "\n");
            if (s.indexOf('\r') >= 0) s = s.substring(s.lastIndexOf('\r') + 1);
            UserInterfacing.printInfo(attribute + " New: " + s);
            if (s.contains("\n")) s = s.substring(0, s.indexOf('\n')) + "...";
            UserInterfacing.setClipStatus(attribute + ": " + (s.length() > 30 ? s.substring(0, 30) + "..." : s));
        } else if (content.hasFiles()) {
            UserInterfacing.setClipStatus(attribute + " Files");
        } else if (content.hasImage()) {
            if (content.hasUrl()) {
                String s = content.getUrl().substring(content.getUrl().lastIndexOf('/') + 1);
                UserInterfacing.printInfo("New " + attribute + " Image: " + s);
                UserInterfacing.setClipStatus(attribute + " Image: " + s);
            }
            UserInterfacing.printInfo("New " + attribute + " Image");
            UserInterfacing.setClipStatus(attribute + " Image");
        } else {
            UserInterfacing.printInfo("New " + attribute + " Content");
            UserInterfacing.setClipStatus(attribute + " Content");
        }
    }

    private static boolean compareImage(Image a, Image b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.getUrl() != null) return a.getUrl().equals(b.getUrl());
        if (b.getUrl() != null) return false;
        if (a.getHeight() != b.getHeight() || a.getWidth() != b.getWidth()) return false;
        PixelReader readerA = a.getPixelReader();
        PixelReader readerB = b.getPixelReader();
        if (!readerA.getPixelFormat().equals(readerB.getPixelFormat())) return false;
        int trials = (int) Math.sqrt(a.getHeight() * a.getWidth()) + 1;
        Random random = new Random();
        for (int i = 0; i < trials; i ++) {
            int x = random.nextInt((int)a.getWidth());
            int y = random.nextInt((int)a.getHeight());
            if (readerA.getArgb(x, y) != readerB.getArgb(x, y)) return false;
        }
        return true;
    }
}
