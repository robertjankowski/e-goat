package datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
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
            waitMilliseconds(1);
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

    public void send(byte[] bytesArray, int offset, InetAddress address, int port) {
        try {
            socket.send(new DatagramPacket(bytesArray, offset, address, port));
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void close() {
        socket.close();
    }


    public static void waitMilliseconds(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
    }

}
