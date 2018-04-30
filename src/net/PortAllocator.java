package net;

import tray.Interfacing;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Allocator for the connection port
 */
public class PortAllocator {

    public synchronized static int alloc() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            System.out.println("Allocated: " + port);
            return port;
        } catch (IOException e) {
            Interfacing.printError(e);
            System.exit(1);
        }
        return 0;
    }
}
