package ui;

import net.FileTransferMode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

class ClipTray {

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
            if (OS.isMac())
                trayIcon = new TrayIcon(ImageIO.read(ClipTray.class.getResourceAsStream("/resources/clip_icon_b.png")));
            else {
                BufferedImage iconImage = ImageIO.read(ClipTray.class.getResourceAsStream("/resources/clip_icon_w.png"));
                int trayIconWidth = new TrayIcon(iconImage).getSize().width;
                trayIcon = new TrayIcon(iconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        Menu statusMenu = new Menu("Status");
        connStatusItem = new MenuItem("Connection status");
        lastItem = new MenuItem("Waiting for Transfer");
        MenuItem warningItem = new MenuItem("Warnings");

        Menu settingMenu = new Menu("Setting");
        CheckboxMenuItem chooserModeItem = new CheckboxMenuItem("Chooser Mode", true);
        CheckboxMenuItem cachedModeItem = new CheckboxMenuItem("Cached Mode");
        MenuItem changeKey = new MenuItem("Change Key");

        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(statusMenu);
        statusMenu.add(connStatusItem);
        statusMenu.add(lastItem);
        statusMenu.add(warningItem);
        warningItem.addActionListener((e) -> LogWindow.toggle());

        popup.add(settingMenu);
        settingMenu.add(chooserModeItem);
        settingMenu.add(cachedModeItem);
        settingMenu.addSeparator();
        settingMenu.add(changeKey);
        changeKey.addActionListener(e -> UserInterfacing.setKey(true));
        chooserModeItem.addItemListener(e -> {
            if (!chooserModeItem.getState()) {
                chooserModeItem.setState(true);
                return;
            }
            cachedModeItem.setState(false);
            FileTransferMode.setLocalMode(FileTransferMode.Mode.CHOOSER);
        });
        cachedModeItem.addItemListener(e -> {
            if (!cachedModeItem.getState()) {
                cachedModeItem.setState(true);
                return;
            }
            chooserModeItem.setState(false);
            FileTransferMode.setLocalMode(FileTransferMode.Mode.CACHED);
        });

        popup.addSeparator();
        popup.add(exitItem);
        exitItem.addActionListener((e) -> System.exit(0));


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
