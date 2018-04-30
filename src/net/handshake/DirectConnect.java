package net.handshake;

import tray.UserInterfacing;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DirectConnect {

    public static InetAddress getTarget(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            UserInterfacing.printInfo("Target " + name + " is unknown");
        }
        return null;
    }
}
