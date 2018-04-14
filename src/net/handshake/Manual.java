package net.handshake;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * implementation of unencrypted handshake
 */
public class Manual {

    private static final int port = 8800;
    private static final String groupAddress = "224.0.0.127"; //"255.255.255.255" for broadcast
    private static MulticastSocket socket;
    private static boolean found;
    private static List<InetAddress> others;

    public static InetAddress getTarget() {
        try {
            others = new ArrayList<>();
            System.out.println("Enter the index of the Address to connect, enter 0 to exit:");
            socket = new MulticastSocket(port);
            socket.joinGroup(InetAddress.getByName(groupAddress));
            Thread tr = new Thread(Manual::receive, "Receiving");
            Thread ts = new Thread(Manual::send, "Sending");
            tr.start();
            ts.start();
            Scanner s = new Scanner(System.in);
            int i = s.nextInt();
            if (i == 0) {
                System.out.println("Connection cancelled. ");
                System.exit(0);
            }
            found = true;
            return others.get(i - 1);
        } catch(InputMismatchException | IndexOutOfBoundsException inError) {
            System.out.println("Cannot Recognize the index. Connection cancelled");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void receive() {
        byte[] received = new byte[33];
        while (!found) {
            try {
                DatagramPacket packet = new DatagramPacket(received, received.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                if (!address.equals(InetAddress.getLocalHost()) && !others.contains(address)) {
                    others.add(address);
                    System.out.println(others.size() + ". " + address);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void send() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] send = new byte[33]; //MAKE MESSAGE
            DatagramPacket packet = new DatagramPacket(send, send.length, InetAddress.getByName("224.0.0.127"), port);
            while (!found) {
                socket.send(packet);
                Thread.sleep(500);
            }
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
