package net.handshake;

import ui.UserInterfacing;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DirectConnect {

    public static InetAddress getTarget(String name) {
        if (name == null) return null;
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            return UserInterfacing.handleConnFail("Computer \"" + name + "\" is unknown");
        }
    }
}
