package server;

import utils.ClientOptions;
import utils.Config;
import utils.User;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UDPServer {

    private static Logger LOGGER = Logger.getLogger(UDPServer.class.getName());
    private static UDPServer udpServer = null;

    private DatagramSocket datagramSocket;
    private DatagramSocket socketClient;
    private DatagramSocket loginSocket;

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
            socketClient = new DatagramSocket();
            loginSocket = new DatagramSocket(Config.LOGIN_PORT);
            datagramSocket = new DatagramSocket(Config.SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        users = Collections.synchronizedList(new ArrayList<>());
        executor = Executors.newFixedThreadPool(1);
    }


    public void runServer() {
        while (true) {
            executor.submit(this::logToServer);
            handleRequests();
        }
    }

    private void logToServer() {
        try {
            var loginPacket = Config.getDatagramPacket();
            loginSocket.receive(loginPacket);
            var login = Config.datagramToString(loginPacket);
            var address = loginPacket.getAddress();
            var newUser = new User(login, address);
            if (!users.contains(newUser)) {
                users.add(newUser);
            }
            var greetings = ("Welcome " + newUser + " to e-goat\t").getBytes();
            socketClient.send(new DatagramPacket(greetings, greetings.length,
                    address, Config.CLIENT_PORT_LISTEN));
            users.forEach(System.out::println);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }

    private void handleRequests() {
        var optionPacket = Config.getDatagramPacket();
        try {
            datagramSocket.receive(optionPacket);
            var choice = Config.datagramToString(optionPacket);
            var userAddr = optionPacket.getAddress();
            var user = users
                    .stream()
                    .filter(u -> u.getAddress() == userAddr)
                    .findFirst()
                    .get();
            var excludedUsers = users
                    .stream()
                    .filter(Predicate.not(u -> u.same(user)))
                    .collect(Collectors.toList());

            switch (ClientOptions.valueOf(choice)) {
                case GET_LIST_OF_FILES:
                    System.out.println("GET_LIST_OF_FILES");
                    for (var u : excludedUsers)
                        setListOfFiles(u);
                    users.forEach(System.out::println);
                    sendListOfFiles(user);
                    break;
                case GET_FILE:
                    System.out.println("GET_FILE");
                    // connect via server to client with desired file
                    break;
                default:
                    // send error code
                    break;
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private void setListOfFiles(User user) throws IOException {
        var address = user.getAddress();
        var message = ClientOptions.GET_LIST_OF_FILES.toString().getBytes();
        socketClient.send(new DatagramPacket(message, message.length, address,
                Config.CLIENT_PORT_LISTEN));

        var nFilesPacket = Config.getDatagramPacket();
        datagramSocket.receive(nFilesPacket);
        var nFiles = Integer.valueOf(Config.datagramToString(nFilesPacket));
        List<String> files = new ArrayList<>();
        for (int i = 0; i < nFiles; i++) {
            var filePacket = Config.getDatagramPacket();
            datagramSocket.receive(filePacket);
            files.add(Config.datagramToString(filePacket));
        }
        users
                .stream()
                .filter(u -> u.same(user))
                .forEach(u -> u.setFiles(files));
    }

    private void sendListOfFiles(User user) {
        var address = user.getAddress();
        var message = users.toString().getBytes();
        try {
            socketClient.send(new DatagramPacket(message, message.length, address,
                    Config.CLIENT_PORT_LISTEN));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
