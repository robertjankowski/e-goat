package server;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import events.User;
import utils.PORT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UDPServer {

    private static Logger LOGGER = Logger.getLogger(UDPServer.class.getName());

    private static UDPServer udpServer = null;

    private UDPSocket socketSend;
    private UDPSocket socketLogin;
    private UDPSocket socketListen;

    private List<User> users;
    private ExecutorService executor;

    public static UDPServer getInstance() {
        if (udpServer == null) {
            udpServer = new UDPServer();
        }
        return udpServer;
    }

    private UDPServer() {
        socketSend = new UDPSocket();
        socketLogin = new UDPSocket(PORT.SERVER_LOGIN);
        socketListen = new UDPSocket(PORT.SERVER_LISTEN);
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
        var loginPacket = socketLogin.receive("Unable to receive login from client");
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
        socketSend.send(welcomeMessage, address, port, "Unable to send login message to client");
    }

    private void listenForRequests() {
        int port = getClientListenPort(socketListen);
    }

    private int getClientListenPort(UDPSocket socket) {
        var packet = socket.receive("Unable to receive message from client");
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
