package utils;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class Config {
    public static final int SERVER_PORT = 8080;
    public static final int LOGIN_PORT = 8888;
    public static final int CLIENT_PORT_LISTEN = 9000;
    public static final int CLIENT_PORT_SEND = 10000;
    public static final int BUFFER_SIZE = 1024;
    public static final String SHARED_FOLDER = "src/client/shared";

    public static DatagramPacket getDatagramPacket() {
        return new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
    }

    public static String datagramToString(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    }

    public static void wait_ms(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
