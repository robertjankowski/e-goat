package server;

import utils.DatagramPacketBuilder;
import utils.Message;
import utils.PORT;
import utils.User;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UDPServer {

    private static Logger LOGGER = Logger.getLogger(UDPServer.class.getName());

    private static UDPServer udpServer = null;

    private DatagramSocket socketSend;
    private DatagramSocket socketListen;

    private List<User> users;
    private ExecutorService executor;

    public static UDPServer getInstance() {
        if (udpServer == null) {
            udpServer = new UDPServer();
        }
        return udpServer;
    }

    private UDPServer() {
        try {
            socketSend = new DatagramSocket();
            socketListen = new DatagramSocket(PORT.SERVER);
        } catch (SocketException ex) {
            LOGGER.severe("Unable to open sockets\t" + ex.getMessage());
        }
        users = new ArrayList<>();
        executor = Executors.newFixedThreadPool(1);
    }


    public void runServer() {
        while (true) {
            executor.submit(this::setDynamicallyPort);
        }
    }

    private void setDynamicallyPort() {
        var initialPacket = DatagramPacketBuilder.create();
        try {
            socketListen.receive(initialPacket);
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive initial message from client\t" + ex.getMessage());
        }
        var message = DatagramPacketBuilder.toString(initialPacket);
        if (Objects.equals(message, Message.INIT_PORT)) {
            String port = String.valueOf(PORT.CURRENT_INIT_COMMUNICATION);
            ++PORT.CURRENT_INIT_COMMUNICATION;
            try {
                socketSend.send(DatagramPacketBuilder.build(port, initialPacket.getAddress(), PORT.INIT_CLIENT));
            } catch (IOException ex) {
                LOGGER.severe("Unable to end initial message to client\t" + ex.getMessage());
            }
            LOGGER.info("New client connected on port: " + port);
        }
    }
}
