package client;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import utils.ClientOptions;
import utils.PORT;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private UDPSocket socketSend;
    private UDPSocket socketListen;
    private int listenPort;

    public UDPClient() {
        listenPort = randomlyPickPort(PORT.LOW_CLIENT_PORT, PORT.HIGH_CLIENT_PORT);
        socketSend = new UDPSocket();
        socketListen = new UDPSocket(listenPort);
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {
            LOGGER.severe("Unable to establish server address");
        }
    }

    public void runClient() {
        if (!logToServer()) {
            LOGGER.info("Unable to login to server\nThe current login name is already in use");
            System.exit(0);
        }
        while (true) {
            // second thread for listening
            var req = requests();
            if (req == ClientOptions.EXIT) {
                break;
            }
        }
    }

    private boolean logToServer() {
        System.out.print("Login: ");
        String login = getMessage();
        socketSend.send(login, serverAddress, PORT.SERVER_LOGIN, "Unable to send initial message to server");
        sendListenPort(PORT.SERVER_LOGIN);
        var loginPacket = socketListen.receive("Unable to receive login message from server");
        String loginMessage = DatagramPacketBuilder.toString(loginPacket);
        if (loginMessage.isEmpty()) {
            return false;
        } else {
            System.out.println(loginMessage);
            return true;
        }
    }

    private ClientOptions requests() {
        ClientOptions option = ClientOptions.NONE;
        while (option != ClientOptions.EXIT) {
            option = selectOption();
            // send port + option to server
            sendListenPort(PORT.SERVER_LISTEN);
        }
        return option;
    }

    private void sendListenPort(int serverPort) {
        String errorMessage = "Unable to send listen port to server";
        socketSend.send(String.valueOf(listenPort), serverAddress, serverPort, errorMessage);
    }

    private int randomlyPickPort(int low, int high) {
        var rand = new Random();
        return low + rand.nextInt(high - low + 1);
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
            LOGGER.log(Level.SEVERE, "Wrong option", ex.getMessage());
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
