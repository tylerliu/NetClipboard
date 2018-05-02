package net.handshake;

import key.KeyUtil;
import ui.UserInterfacing;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * set failed connection warning
 * use random + IP + MAC to check
 * or use static key
 * need to be fast, just small check
 */
public class KeyBased {
    private static final int WAIT_TIME = 30000; //in milli-second
    private static final int port = 8800;
    private static final String groupAddress = "224.0.0.127"; //"255.255.255.255" for broadcast
    private static final AtomicReference<InetAddress> target = new AtomicReference<>();
    private static MulticastSocket socket;
    private static DatagramSocket sendSocket;
    private static ConcurrentHashMap<InetAddress, Boolean> responded;
    private static ConcurrentHashMap<InetAddress, Boolean> authenticated;
    private static byte[] ran = new byte[KeyUtil.KEY_LEN];
    private static byte[] key = new byte[KeyUtil.KEY_LEN];
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static InetAddress getTarget() {
        initHandShake();
        executorService.submit(KeyBased::receive);
        executorService.submit(KeyBased::send);
        boolean result = monitor();
        executorService.shutdown();
        CompletableFuture.runAsync(() -> {
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
                socket.close();
                sendSocket.close();
            } catch (InterruptedException e) {
                UserInterfacing.printError(e);
            }
        });
        if (!result) {
            UserInterfacing.printInfo("Fail To Find Connection Target");
            UserInterfacing.printInfo("Connection Failed");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }
        return target.get();
    }

    private static boolean monitor() {
        long start_time = System.currentTimeMillis();
        while (target.get() == null && System.currentTimeMillis() - start_time <= WAIT_TIME) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                UserInterfacing.printError(e);
            }
        }
        if (target.get() == null) {
            try {
                target.set(InetAddress.getLocalHost());
            } catch (UnknownHostException e) {
                UserInterfacing.printError(e);
            }
            return false;
        } else {
            return true;
        }
    }

    private static void initHandShake() {
        try {
            randomize();
            getKey();
            responded = new ConcurrentHashMap<>();
            responded.put(InetAddress.getLocalHost(), true); //prevent connecting itself
            authenticated = new ConcurrentHashMap<>();
            authenticated.put(InetAddress.getLocalHost(), false); //prevent connecting itself
            socket = new MulticastSocket(port);
            sendSocket = new DatagramSocket();
            socket.joinGroup(InetAddress.getByName(groupAddress));
        } catch (IOException e) {
            UserInterfacing.printError(e);
            System.exit(1);
        }
    }

    private static void randomize() {
        try {
            SecureRandom.getInstanceStrong().nextBytes(ran);
        } catch (NoSuchAlgorithmException e) {
            UserInterfacing.printError(e);
        }
    }

    private static void getKey() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-256");
            md.update("Handshake key".getBytes());
            key = md.digest(KeyUtil.getKey());
        } catch (NoSuchAlgorithmException e) {
            UserInterfacing.printError(e);
        }
    }

    /**
     * receiving and determine the best action for each packet
     */
    private static void receive() {
        while (true) {
            try {
                byte[] received = new byte[33];
                DatagramPacket packet = new DatagramPacket(received, received.length);
                socket.receive(packet);
                if (target.get() != null) return;
                if (packet.getData()[0] == 0) {
                    executorService.submit(() -> respond(packet));
                } else if (packet.getData()[0] == -1) {
                    executorService.submit(() -> auth(packet));
                }
            } catch (Exception e) {
                UserInterfacing.printError(e);
            }
        }
    }

    private static byte[] HMAC(InetAddress address, byte[] random) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            mac.update(address.getAddress());
            return mac.doFinal(random);
        } catch (Exception e) {
            UserInterfacing.printError(e);
        }
        return new byte[0];
    }

    /**
     * send response from for given packet
     *
     * @param packet the received packet
     */
    private static void respond(DatagramPacket packet) {
        if (Boolean.TRUE.equals(responded.get(packet.getAddress())) || Boolean.FALSE.equals(authenticated.get(packet.getAddress())))
            return;
        if (Boolean.TRUE.equals(authenticated.get(packet.getAddress())) && !target.compareAndSet(null, packet.getAddress()))
            return;
        responded.put(packet.getAddress(), true);
        UserInterfacing.printInfo("responding " + packet.getAddress());
        byte[] random = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
        try {
            byte[] response = ByteBuffer.allocate(1 + 32)
                    .put((byte) -1)
                    .put(HMAC(InetAddress.getLocalHost(), random))
                    .array();

            for (int i = 0; i < 4; i++) {
                DatagramPacket rePacket = new DatagramPacket(response, response.length, packet.getAddress(), port);
                sendSocket.send(rePacket);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            UserInterfacing.printError(e);
        }
    }

    /**
     * authorize certain packet
     *
     * @param packet the received packet
     */
    private static void auth(DatagramPacket packet) {
        if (authenticated.get(packet.getAddress()) != null) return;
        UserInterfacing.printInfo("authenticating " + packet.getAddress());
        byte[] correct = ByteBuffer.allocate(1 + 32)
                .put((byte) -1)
                .put(HMAC(packet.getAddress(), ran))
                .array();
        if (!Arrays.equals(correct, packet.getData())) {
            authenticated.put(packet.getAddress(), false);
        } else {
            authenticated.put(packet.getAddress(), true);
            if (Boolean.TRUE.equals(responded.get(packet.getAddress()))) {
                target.compareAndSet(null, packet.getAddress());
            }
        }
    }

    private static void send() {
        try {
            SecureRandom.getInstanceStrong().nextBytes(ran);
            sendSocket.setBroadcast(true);
            byte[] send = ByteBuffer.allocate(ran.length + 1).put((byte) 0).put(ran).array();
            DatagramPacket packet = new DatagramPacket(send, send.length, InetAddress.getByName(groupAddress), port);

            while (target.get() == null) {
                sendSocket.send(packet);
                Thread.sleep(1000);
            }
            sendSocket.send(packet); //last send to unclog receive
        } catch (Exception e) {
            UserInterfacing.printError(e);
        }
    }
}
