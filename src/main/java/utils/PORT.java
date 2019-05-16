package utils;

import java.util.Random;

public final class PORT {
    public static final int SERVER_PRODUCER = 9999;
    public static final int SERVER_CONSUMER = 9998;
    public static final int INIT_CLIENT = 20000;
    private static final int LOW_CLIENT_PORT = 40000;
    private static final int HIGH_CLIENT_PORT = 60000;

    public static int getRandomPort() {
        Random rand = new Random();
        return LOW_CLIENT_PORT + rand.nextInt(HIGH_CLIENT_PORT - LOW_CLIENT_PORT + 1);
    }

    public static String getRandomPortString() {
        return String.valueOf(getRandomPort());
    }
}
