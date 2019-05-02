package client;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import message.Message;
import user.User;
import utils.ClientOptions;
import utils.PORT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        while (!login()) {
            waitMilisecond();
        }
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
        var doesUserExists = DatagramPacketBuilder.receiveAndReturnString(socketListenPort1);
        if (Boolean.valueOf(doesUserExists)) {
            System.out.println("User with the same login already exists, try again");
        }
        var welcomeMessage = DatagramPacketBuilder.receiveAndReturnString(socketListenPort1);
        System.out.println(welcomeMessage);
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
        return DatagramPacketBuilder.receiveAndReturnString(socketListenPort1);
    }

    private void downloadFile() {
        socketSend.send(Message.DOWNLOAD, serverAddress, PORT.SERVER_PRODUCER);
        socketSend.send(login, serverAddress, PORT.SERVER_CONSUMER);

        var loginFile = getFileAndUserLogin();
        socketSend.send(loginFile, serverAddress, PORT.SERVER_CONSUMER);

        if (!validateRequests("User with this name does not exist!")) {
            return;
        }
        if (!validateRequests("User does not have file with this name!")) {
            return;
        }

        var fileName = DatagramPacketBuilder.receiveAndReturnString(socketListenPort1);
        var file = getFileWithPath(fileName);
        try (var fos = new FileOutputStream(file)) {
            while (true) {
                ByteBuffer bf = ByteBuffer.wrap(socketListenPort1.receive().getData());
                int length = bf.getInt();
                if (length == 0)
                    break;
                byte[] byteArray = socketListenPort1.receive().getData();
                fos.write(byteArray, 0, length);
            }
            fos.flush();
        } catch (IOException e) {
            LOGGER.severe("Unable to initialize file " + e.getMessage());
        }
    }

    private boolean validateRequests(String errorMessage) {
        var isValidate = DatagramPacketBuilder.receiveAndReturnString(socketListenPort1);
        if (!Boolean.valueOf(isValidate)) {
            System.out.println(errorMessage);
            return false;
        }
        return true;
    }

    private String getFileAndUserLogin() {
        System.out.print("Choose login: ");
        var userLogin = getMessage();
        System.out.print("Choose name of file: ");
        var fileName = getMessage();
        return userLogin + "," + fileName;
    }

    private void listen() {
        var message = DatagramPacketBuilder.receiveAndReturnString(socketListenPort2);
        switch (message) {
            case Message.FILE_LIST:
                returnFileList();
                break;
            case Message.DOWNLOAD:
                try {
                    returnFile();
                } catch (UnknownHostException e) {
                    LOGGER.severe("Cannot established client address " + e.getMessage());
                }
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

    private void returnFile() throws UnknownHostException {
        var portToSend = DatagramPacketBuilder.receiveAndReturnString(socketListenPort2);
        var addrToSend = DatagramPacketBuilder.receiveAndReturnString(socketListenPort2);
        InetAddress addr = InetAddress.getByName(addrToSend);
        var fileName = DatagramPacketBuilder.receiveAndReturnString(socketListenPort2);
        var fileExists = User.getFiles().stream()
                .anyMatch(file -> Objects.equals(file, fileName));
        if (!fileExists) {
            sendToClient("false", addr, portToSend);
            return;
        }
        sendToClient("true", addr, portToSend);
        var newFile = login + "_" + fileName;
        sendToClient(newFile, addr, portToSend);

        var file = getFileWithPath(fileName);
        try (var fis = new FileInputStream(file)) {
            int count;
            var byteArray = new byte[DatagramPacketBuilder.BUFFER_SIZE];
            while ((count = fis.read(byteArray)) != -1) {
                var lengthBytes = ByteBuffer.allocate(4).putInt(count).array();
                socketSend.send(lengthBytes, 4, addr, Integer.valueOf(portToSend));
                waitMilisecond();
                socketSend.send(byteArray, count, addr, Integer.valueOf(portToSend));
                waitMilisecond();
            }
        } catch (IOException e) {
            LOGGER.severe("Unable to initialize file " + e.getMessage());
        }
        byte[] lengthBytes = {0};
        socketSend.send(lengthBytes, 1, addr, Integer.valueOf(portToSend));
    }

    private void sendToClient(String message, InetAddress address, String port) {
        socketSend.send(message, address, Integer.valueOf(port));
    }

    private File getFileWithPath(String fileName) {
        return FileSystems.getDefault()
                .getPath(User.SHARED_FOLDER + "/" + fileName)
                .toAbsolutePath()
                .toFile();
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

    private void waitMilisecond() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void printOptions() {
        System.out.println("\n\tOPTIONS");
        System.out.println("\t1. Get list of available files");
        System.out.println("\t2. Get file");
        System.out.println("\t3. Exit\n");
    }
}
