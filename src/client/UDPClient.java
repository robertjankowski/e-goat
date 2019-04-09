package client;

import utils.Config;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socket;


    public UDPClient() throws UnknownHostException, SocketException {
        serverAddress = InetAddress.getByName("localhost");
        socket = new DatagramSocket();
    }

    private String getMessage() {
        var input = new Scanner(System.in);
        return input.next();
    }

    public void runClient(boolean once) throws IOException {
        do {
            LOGGER.info("Server address: " + serverAddress);
            var content = getMessage().getBytes();
            var sentPacket = new DatagramPacket(content, content.length);
            sentPacket.setAddress(serverAddress);
            sentPacket.setPort(Config.PORT);
            socket.send(sentPacket);

            var receivedPacket = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
            socket.setSoTimeout(1000);
            socket.receive(receivedPacket);
            LOGGER.info("Server received message");
        } while (!once);
    }
}
