package server;

import utils.DatagramPacketBuilder;
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
    private DatagramSocket socketLogin;
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
            socketLogin = new DatagramSocket(PORT.SERVER_LOGIN);
            socketListen = new DatagramSocket(PORT.SERVER_LISTEN);
        } catch (SocketException ex) {
            LOGGER.severe("Unable to open sockets\t" + ex.getMessage());
        }
        users = new ArrayList<>();
        executor = Executors.newFixedThreadPool(1);
    }


    public void runServer() {
        while (true) {
            executor.submit(this::logToServer);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void logToServer() {
        var loginPacket = DatagramPacketBuilder.create();
        try {
            socketLogin.receive(loginPacket);
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive login from client\t" + ex.getMessage());
        }
        String login = DatagramPacketBuilder.toString(loginPacket);
        String welcomeMessage;
        int port = getClientListenPort(socketLogin);
        var address = loginPacket.getAddress();
        if (!doesUserExist(login)) {
            var newUser = new User(login, address, port);
            users.add(newUser);
            welcomeMessage = buildWelcomeMessage(newUser);
            LOGGER.info(newUser + " logged into server");
        } else {
            welcomeMessage = "";
        }
        try {
            socketSend.send(DatagramPacketBuilder.build(welcomeMessage, address, port));
        } catch (IOException ex) {
            LOGGER.severe("Unable to send login message to client\t" + ex.getMessage());
        }
    }

    private void listenForRequests() {
        int port = getClientListenPort(socketListen);
    }

    private int getClientListenPort(DatagramSocket socket) {
        var packet = DatagramPacketBuilder.create();
        try {
            socket.receive(packet);
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive message from client\t" + ex.getMessage());
        }
        return DatagramPacketBuilder.toInt(packet);
    }

    private boolean doesUserExist(String login) {
        var userExists = users
                .stream()
                .filter(u -> Objects.equals(u.getLogin(), login))
                .findFirst()
                .orElse(null);
        return userExists != null;
    }

    private String buildWelcomeMessage(User user) {
        String message = "\n\tWelcome on e-goat server\n";
        return message + "\t" + user.toString();
    }

}
