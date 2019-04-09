package server;

import utils.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    private static UDPServer udpServer = null;

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    private UDPServer() throws SocketException {
        datagramSocket = new DatagramSocket(Config.PORT);
        datagramPacket = getDatagramPacket();
    }

    public static UDPServer getUdpServer() throws SocketException {
        if (udpServer == null) {
            udpServer = new UDPServer();
        }
        return udpServer;
    }

    public void runServer() throws IOException {
        var byteResponse = "OK".getBytes();
        while (true) {
            datagramPacket = getDatagramPacket();
            datagramSocket.receive(datagramPacket);

            int length = datagramPacket.getLength();
            String message = new String(datagramPacket.getData(), 0, length, StandardCharsets.UTF_8);

            var address = datagramPacket.getAddress();
            int port = datagramPacket.getPort();

            System.out.println("Message: " + message);
            var response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
            datagramSocket.send(response);
        }
    }

    private DatagramPacket getDatagramPacket() {
        return new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
    }
}
