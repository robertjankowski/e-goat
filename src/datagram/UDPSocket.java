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

    public DatagramPacket receive(int timeout) {
        var packet = DatagramPacketBuilder.create();
        try {
            socket.receive(packet);
            socket.setSoTimeout(timeout);
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive message" + "\n" + ex.getMessage());
        }
        return packet;
    }

    public DatagramPacket receive() {
        return receive(0);
    }

    public void send(String message, InetAddress address, int port, String errorMessage) {
        try {
            socket.send(DatagramPacketBuilder.build(message, address, port));
        } catch (IOException ex) {
            LOGGER.severe(errorMessage + "\n" + ex.getMessage());
        }
    }

    public void send(String message, InetAddress address, int port) {
        send(message, address, port, "Unable to send message\n");
    }

    public void close() {
        socket.close();
    }

}
