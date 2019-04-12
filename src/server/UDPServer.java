package server;

import utils.Config;
import utils.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UDPServer {

    private static UDPServer udpServer = null;

    private DatagramSocket datagramSocket;
    private DatagramSocket socketClient;
    private DatagramSocket loginSocket;

    private List<User> users;

    public static UDPServer getInstance() {
        if (udpServer == null) {
            udpServer = new UDPServer();
        }
        return udpServer;
    }

    private UDPServer() {
        try {
            socketClient = new DatagramSocket();
            loginSocket = new DatagramSocket(Config.LOGIN_PORT);
            datagramSocket = new DatagramSocket(Config.SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        users = new ArrayList<>();
    }


    public void runServer() throws IOException {
        while (true) {
            var loginPacket = Config.getDatagramPacket();
            loginSocket.receive(loginPacket);
            var login = new String(loginPacket.getData(), 0, loginPacket.getLength(), StandardCharsets.UTF_8);
            var address = loginPacket.getAddress();
            var newUser = new User(login, address);
            if (!users.contains(newUser)) {
                users.add(newUser);
            }
            var greetings = ("Welcome " + newUser + " to e-goat\t").getBytes();
            socketClient.send(new DatagramPacket(greetings, greetings.length, address, Config.CLIENT_PORT_LISTEN));
            users.forEach(System.out::println);


        }
    }
}
