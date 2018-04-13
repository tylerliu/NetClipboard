package net.handshake;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DirectConnect {

    public static InetAddress getTarget(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            System.out.println("Target " + name + " is unknown");
        }
        return null;
    }
}
