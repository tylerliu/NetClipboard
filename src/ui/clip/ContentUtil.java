package ui.clip;

import javafx.scene.input.ClipboardContent;
import ui.UserInterfacing;

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
        if (a.hasString() && !a.getString().equals(b.getString())) return false;
        if (a.hasHtml() && !a.getHtml().equals(b.getHtml())) return false;
        if (a.hasRtf() && !a.getRtf().equals(b.getRtf())) return false;
        if (a.hasUrl() && !a.getUrl().equals(b.getUrl())) return false;

        //TODO image
        return true;
    }

    public static void printContent(ClipboardContent content, String attribute) {
        if (content.hasFiles()) {
            UserInterfacing.printInfo(attribute + " New: " + content.getFiles());
        }
        if (content.hasString()) {
            String s = content.getString();
            if (s.indexOf('\r') >= 0) s = s.substring(s.lastIndexOf('\r') + 1);
            UserInterfacing.printInfo(attribute + " New: " + s);
            if (s.contains("\n")) s = s.substring(0, s.indexOf('\n')) + "...";
            UserInterfacing.setClipStatus(attribute + ": " + (s.length() > 30 ? s.substring(0, 30) + "..." : s));
        } else if (content.hasFiles()) {
            UserInterfacing.setClipStatus(attribute + " Files");
        } else if (content.hasImage()) {
            UserInterfacing.printInfo("New " + attribute + " Image");
            UserInterfacing.setClipStatus("New " + attribute + " Image");
        } else {
            UserInterfacing.printInfo("New " + attribute + " Content");
            UserInterfacing.setClipStatus("New " + attribute + " Content");
        }
    }
}
