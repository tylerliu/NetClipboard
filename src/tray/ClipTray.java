package tray;

import clip.MacFilesClipboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class ClipTray {

    private static MenuItem connStatusItem;
    private static MenuItem lastItem;

    static void init() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        TrayIcon trayIcon;
        try {
            if (MacFilesClipboard.isMac())
                trayIcon = new TrayIcon(ImageIO.read(ClipTray.class.getResourceAsStream("/resources/clip_icon_b.png")));
            else
                trayIcon = new TrayIcon(ImageIO.read(ClipTray.class.getResourceAsStream("/resources/clip_icon_w.png")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        Menu statusMenu = new Menu("Status");
        connStatusItem = new MenuItem("Connection status");
        MenuItem warningItem = new MenuItem("Warnings");
        lastItem = new MenuItem("Waiting for Transfer");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(statusMenu);
        statusMenu.add(connStatusItem);
        statusMenu.add(lastItem);
        statusMenu.add(warningItem);
        popup.add(exitItem);
        exitItem.addActionListener((e) -> System.exit(0));
        warningItem.addActionListener((e) -> LogWindow.getLogWindow().toggle());

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    static void setStatus(String s) {
        if (connStatusItem != null) connStatusItem.setLabel(s);
    }

    static void setLastItem(String s) {
        if (lastItem != null) lastItem.setLabel(s);
    }
}
