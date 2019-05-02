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
        var loginPacket = socket.receive();
        user.setLogin(DatagramPacketBuilder.toString(loginPacket));
        var isUserExists = users.stream()
                .anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (isUserExists) {
            sendMessageToUser(user, "true");
            return;
        }
        sendMessageToUser(user, "false");
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
            sendMessageToUser(askUser, "false");
            return;
        }
        boolean doesUserExists = users.stream()
                .anyMatch(u -> u.getLogin().equals(user.getLogin()));
        if (!doesUserExists) {
            sendMessageToUser(askUser, "false");
            return;
        }
        sendMessageToUser(askUser, "true");

        socket.send(Message.DOWNLOAD, user.getAddress(), Integer.valueOf(user.getRandomPort2()));
        socket.send(askUser.getRandomPort1(), user.getAddress(), Integer.valueOf(user.getRandomPort2()));
        socket.send(askUser.getAddress().getHostName(), user.getAddress(), Integer.valueOf(user.getRandomPort2()));
        socket.send(fileName, user.getAddress(), Integer.valueOf(user.getRandomPort2()));
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
        sendMessageToUser(user, message);
    }

    private void sendMessageToUser(User user, String message) {
        socket.send(message, user.getAddress(), Integer.valueOf(user.getRandomPort1()));
    }

    private List<User> getExcludedUsers(User user) {
        return users.stream()
                .filter(Predicate.not(u -> u.sameUser(user)))
                .collect(Collectors.toList());
    }

    private List<String> getFilesFromUser(User user) {
        socket.send(Message.FILE_LIST, user.getAddress(), Integer.valueOf(user.getRandomPort2()));
        var files = DatagramPacketBuilder.receiveAndReturnString(socket);
        return User.listOfFileToList(files);
    }

    private String buildListOfFiles(List<User> excludedUsers) {
        return excludedUsers.stream()
                .map(User::showListOfFiles)
                .collect(Collectors.joining("\n"));
    }

    private String buildWelcomeMessage(User user) {
        String message = "\n\tWelcome on e-goat server\n";
        return message + "\t" + user.toString();
    }
}
