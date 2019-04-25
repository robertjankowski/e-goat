package datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class UDPSocket {

    private final static Logger LOGGER = Logger.getLogger(UDPSocket.class.getName());
    private DatagramSocket socket;

    public UDPSocket() {
        try {
            socket = new DatagramSocket();
        } catch (IOException ex) {
            LOGGER.severe("Unable to initialize socket");
        }
    }

    public UDPSocket(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (IOException ex) {
            LOGGER.severe("Unable to initialize socket");
        }
    }

    public DatagramPacket receive(String errorMessage) {
        var packet = DatagramPacketBuilder.create();
        try {
            socket.receive(packet);
        } catch (IOException ex) {
            LOGGER.severe(errorMessage + "\n" + ex.getMessage());
        }
        return packet;
    }

    public void send(String message, InetAddress address, int port, String errorMessage) {
        try {
            socket.send(DatagramPacketBuilder.build(message, address, port));
        } catch (IOException ex) {
            LOGGER.severe(errorMessage + "\n" + ex.getMessage());
        }
    }

}
