package utils;

import java.net.DatagramPacket;

public final class Config {
    public static final int SERVER_PORT = 8080;
    public static final int LOGIN_PORT = 8888;
    public static final int CLIENT_PORT_LISTEN = 9000;
    public static final int CLIENT_PORT_SEND = 10000;
    public static final int BUFFER_SIZE = 1024;

    public static DatagramPacket getDatagramPacket() {
        return new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
    }
}
