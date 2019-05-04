package datagram;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class DatagramPacketBuilder {

    public static final int BUFFER_SIZE = 1024;
    private static final Logger LOGGER = Logger.getLogger(DatagramPacketBuilder.class.getName());

    public static DatagramPacket build(String message, InetAddress address, int port) {
        var messageBytes = message.getBytes();
        return new DatagramPacket(messageBytes, messageBytes.length, address, port);
    }

    public static DatagramPacket create() {
        return new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
    }

    public static String toString(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    }

    public static int toInt(DatagramPacket packet) {
        var message = toString(packet);
        try {
            return Integer.valueOf(message);
        } catch (NumberFormatException ex) {
            LOGGER.severe("Unable to convert datagramPacket to int\t" + ex.getMessage());
        }
        return -1;
    }

    public static String receiveAndReturnString(UDPSocket socket) {
        return DatagramPacketBuilder.toString(socket.receive());
    }
}
