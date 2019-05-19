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

    public ServerConsumer(ArrayBlockingQueue<Message> eventsQueue, int port, String ip) {
        super(eventsQueue, port, ip);
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

    /**
     * Get the message from the queue and handle it
     * LOGIN       -   get login from the client and check if the user already exists
     * FILE_LIST   -   get from all users (except the asking ones) all the files and return them
     * DOWNLOAD    -   receive filename and username and validate them
     *                 send to client (listening mode) port, address, and filename the client who was asking
     * EXIT        -   delete user from the list
     * @param message - an event type to handle
     */
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
                handleDownloadFile();
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
        var login = DatagramPacketBuilder.receiveAndReturnString(socket);
        user.setLogin(login);
        var isUserExists = users.stream()
                .anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (isUserExists) {
            sendMessageToUserRequest(user, "true");
            return;
        }
        sendMessageToUserRequest(user, "false");
        sendWelcomeMessage(user);
        users.add(user);
    }

    private void handleFileList() {
        var loginPacket = socket.receive();
        var loginName = DatagramPacketBuilder.toString(loginPacket);
        var user = getUserWithLoginName(loginName);
        var excludedUsers = getExcludedUsers(user);
        excludedUsers.forEach(u -> u.setListOfFiles(getFilesFromUser(u)));
        var allFiles = buildListOfFiles(excludedUsers);
        socket.send(allFiles, user.getAddress(), Integer.valueOf(user.getRandomPort1()));
    }

    private void handleDownloadFile() {
        var askUserLogin = DatagramPacketBuilder.receiveAndReturnString(socket);
        var askUser = getUserWithLoginName(askUserLogin);

        var loginFileName = receiveLoginAndFilename();
        String login = loginFileName[0];
        String fileName = loginFileName[1];

        var user = getUserWithLoginName(login);
        if (user.getLogin().isEmpty()) {
            sendMessageToUserRequest(askUser, "false");
            return;
        }
        boolean doesUserExists = users.stream()
                .anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (!doesUserExists) {
            sendMessageToUserRequest(askUser, "false");
            return;
        }
        sendMessageToUserRequest(askUser, "true");

        sendMessageToListenUser(user, Message.DOWNLOAD);
        sendMessageToListenUser(user, askUser.getRandomPort1());
        sendMessageToListenUser(user, askUser.getAddress().getHostName());
        sendMessageToListenUser(user, fileName);
    }

    private String[] receiveLoginAndFilename() {
        return DatagramPacketBuilder.receiveAndReturnString(socket).split(",");
    }

    private User getUserWithLoginName(String login) {
        return users.stream()
                .filter(u -> u.getLogin().equals(login))
                .findFirst()
                .orElseGet(User::new);
    }

    private void removeUserFromList() {
        var toRemovedUser = DatagramPacketBuilder.receiveAndReturnString(socket);
        users.removeIf(u -> u.getLogin().equals(toRemovedUser));
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
        sendMessageToUserRequest(user, message);
    }

    private void sendMessageToUser(User user, String message, String port) {
        socket.send(message, user.getAddress(), Integer.valueOf(port));
    }

    private void sendMessageToUserRequest(User user, String message) {
        sendMessageToUser(user, message, user.getRandomPort1());
    }

    private void sendMessageToListenUser(User user, String message) {
        sendMessageToUser(user, message, user.getRandomPort2());
    }

    private List<User> getExcludedUsers(User user) {
        return users.stream()
                .filter(Predicate.not(u -> u.sameUser(user)))
                .collect(Collectors.toList());
    }

    private List<String> getFilesFromUser(User user) {
        sendMessageToListenUser(user, Message.FILE_LIST);
        var files = DatagramPacketBuilder.receiveAndReturnString(socket);
        return User.listOfFileToList(files);
    }

    private String buildListOfFiles(List<User> excludedUsers) {
        return excludedUsers.stream()
                .map(User::showListOfFiles)
                .collect(Collectors.joining("\n"));
    }

    private String buildWelcomeMessage(User user) {
        return "\n\tWelcome on e-goat server\n\t" + user.toString();
    }
}
