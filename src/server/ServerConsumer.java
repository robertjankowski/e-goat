package server;

import datagram.DatagramPacketBuilder;
import message.Message;
import user.User;
import utils.PORT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServerConsumer extends Server {

    private static final Logger LOGGER = Logger.getLogger(ServerConsumer.class.getName());
    private List<User> users;

    public ServerConsumer(ArrayBlockingQueue<Message> eventsQueue, int port) {
        super(eventsQueue, port);
        users = new ArrayList<>();
    }

    public void run() {
        try {
            var event = eventsQueue.take();
            handleEvent(event);
        } catch (InterruptedException ex) {
            LOGGER.severe("Error in processing events" + ex.getMessage());
        }
    }

    private void handleEvent(Message message) {
        System.out.println(message.getMessage());
        switch (message.getMessage()) {
            case Message.LOGIN:
                handleLogin(message.getUser());
                break;
            case Message.FILE_LIST:
                handleFileList();
                break;
            case Message.DOWNLOAD:
                break;
            case Message.EXIT:
                removeUserFromList();
                break;
            default:
                LOGGER.severe("Undefined message type");
                break;
        }
    }

    private void handleLogin(User user) {
        sendRandomPorts(user);
        var loginPacket = socket.receive();
        user.setLogin(DatagramPacketBuilder.toString(loginPacket));
        sendWelcomeMessage(user);
        users.add(user);
    }

    private void sendRandomPorts(User user) {
        String randomPort1 = PORT.getRandomPortString();
        String randomPort2 = PORT.getRandomPortString();
        String errorMessage = "Unable to send random port to client";
        socket.send(randomPort1, user.getAddress(), PORT.INIT_CLIENT, errorMessage);
        socket.send(randomPort2, user.getAddress(), PORT.INIT_CLIENT, errorMessage);
        user.setRandomPort1(randomPort1);
        user.setRandomPort2(randomPort2);
    }

    private void sendWelcomeMessage(User user) {
        String message = buildWelcomeMessage(user);
        socket.send(message, user.getAddress(), Integer.valueOf(user.getRandomPort1()));
    }

    private void handleFileList() {
        var loginPacket = socket.receive();
        var loginName = DatagramPacketBuilder.toString(loginPacket);
        var user = users.stream()
                .filter(u -> u.getLogin().equals(loginName))
                .findFirst().orElseThrow();
        var excludedUsers = getExcludedUsers(user);
        excludedUsers.forEach(u -> u.setListOfFiles(getFilesFromUser(u)));
        var allFiles = buildListOfFiles(excludedUsers);
        socket.send(allFiles, user.getAddress(), Integer.valueOf(user.getRandomPort1()));
    }

    private List<User> getExcludedUsers(User user) {
        return users.stream()
                .filter(Predicate.not(u -> u.sameUser(user)))
                .collect(Collectors.toList());
    }

    private List<String> getFilesFromUser(User user) {
        socket.send(Message.FILE_LIST, user.getAddress(), Integer.valueOf(user.getRandomPort2()));
        var filesPacket = socket.receive();
        var files = DatagramPacketBuilder.toString(filesPacket);
        return User.listOfFileToList(files);
    }

    private String buildListOfFiles(List<User> excludedUsers) {
        return excludedUsers.stream()
                .map(User::showListOfFiles)
                .collect(Collectors.joining("\n"));
    }

    private void removeUserFromList() {
        var toRemovedUserPacket = socket.receive();
        var toRemovedUser = DatagramPacketBuilder.toString(toRemovedUserPacket);
        users.removeIf(u -> u.getLogin().equals(toRemovedUser));
        users.forEach(System.out::println);
    }

    private String buildWelcomeMessage(User user) {
        String message = "\n\tWelcome on e-goat server\n";
        return message + "\t" + user.toString();
    }
}
