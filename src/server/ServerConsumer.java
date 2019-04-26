package server;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import message.Message;
import user.User;
import utils.PORT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerConsumer extends Server {

    private UDPSocket socket;
    private List<User> users;

    public ServerConsumer(ArrayBlockingQueue<Message> eventsQueue) {
        super(eventsQueue);
        socket = new UDPSocket(PORT.SERVER_CONSUMER);
        users = new ArrayList<>();
    }

    public void run() {
        try {
            var event = eventsQueue.take();
            handleEvent(event);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void handleEvent(Message message) {
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
    }

    private String buildWelcomeMessage(User user) {
        String message = "\n\tWelcome on e-goat server\n";
        return message + "\t" + user.toString();
    }


}
