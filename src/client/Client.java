package client;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import message.Message;
import user.User;
import utils.ClientOptions;
import utils.PORT;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Client {

    private static Logger LOGGER = Logger.getLogger(Client.class.getName());

    private InetAddress serverAddress;
    private UDPSocket socketSend;
    private UDPSocket socketListenPort1;
    private UDPSocket socketListenPort2;
    private String login;
    private ExecutorService executor;

    public Client() {
        try {
            serverAddress = InetAddress.getByName("192.168.0.25");
        } catch (UnknownHostException ex) {
            LOGGER.severe("Unable to establish server address");
        }
        socketSend = new UDPSocket();
        executor = Executors.newSingleThreadExecutor();
    }

    public void runClient() {
        login();
        while (true) {
            executor.submit(this::listen);
            sendRequests();
        }
    }

    private boolean login() {
        socketSend.send(Message.LOGIN, serverAddress, PORT.SERVER_PRODUCER);
        receiveRandomPorts();
        System.out.print("Login: ");
        login = getMessage();
        socketSend.send(login, serverAddress, PORT.SERVER_CONSUMER);
        System.out.println(receiveWelcomeMessage());
        return true;
    }

    private void receiveRandomPorts() {
        var socket = new UDPSocket(PORT.INIT_CLIENT);
        int randomPort1 = DatagramPacketBuilder.toInt(socket.receive());
        int randomPort2 = DatagramPacketBuilder.toInt(socket.receive());
        socket.close();
        socketListenPort1 = new UDPSocket(randomPort1);
        socketListenPort2 = new UDPSocket(randomPort2);
    }

    private String receiveWelcomeMessage() {
        var message = socketListenPort1.receive();
        return DatagramPacketBuilder.toString(message);
    }

    private void sendRequests() {
        var option = selectOption();
        switch (option) {
            case FILE_LIST:
                System.out.println(filesList());
                break;
            case DOWNLOAD:
                downloadFile();
                break;
            case EXIT:
                sendGoodBye();
                System.exit(0);
            default:
                LOGGER.severe("Wrong option, try again");
        }
    }

    private void sendGoodBye() {
        socketSend.send(Message.EXIT, serverAddress, PORT.SERVER_PRODUCER);
        socketSend.send(login, serverAddress, PORT.SERVER_CONSUMER);
    }

    private String filesList() {
        socketSend.send(Message.FILE_LIST, serverAddress, PORT.SERVER_PRODUCER);
        socketSend.send(login, serverAddress, PORT.SERVER_CONSUMER);
        var files = socketListenPort1.receive();
        return DatagramPacketBuilder.toString(files);
    }

    private void downloadFile() {
        socketSend.send(Message.DOWNLOAD, serverAddress, PORT.SERVER_PRODUCER);
        var loginFile = getFileAndUserLogin();
        socketSend.send(loginFile, serverAddress, PORT.SERVER_CONSUMER);

    }

    private String getFileAndUserLogin() {
        System.out.print("Choose login: ");
        var userLogin = getMessage();
        System.out.flush();
        System.out.print("Choose name of file: ");
        var fileName = getMessage();
        return userLogin + "," + fileName;
    }

    private void listen() {
        var messagePacket = socketListenPort2.receive();
        String message = DatagramPacketBuilder.toString(messagePacket);
        switch (message) {
            case Message.FILE_LIST:
                returnFileList();
                break;
            case Message.DOWNLOAD:
                returnFile();
                break;
            default:
                break;
        }
    }

    private void returnFileList() {
        List<String> files = User.getFiles();
        String joinedFiles = User.listOfFilesToString(files);
        socketSend.send(joinedFiles, serverAddress, PORT.SERVER_CONSUMER);
    }

    private void returnFile() {

    }

    private String getMessage() {
        var input = new Scanner(System.in);
        return input.next();
    }

    private ClientOptions selectOption() {
        printOptions();
        try {
            int choice = Integer.valueOf(getMessage());
            return ClientOptions.fromId(choice);
        } catch (NumberFormatException ex) {
            LOGGER.severe("Wrong option" + ex.getMessage());
        }
        return ClientOptions.NONE;
    }

    private void printOptions() {
        System.out.println("\n\tOPTIONS");
        System.out.println("\t1. Get list of available files");
        System.out.println("\t2. Get file");
        System.out.println("\t3. Exit\n");
    }
}
