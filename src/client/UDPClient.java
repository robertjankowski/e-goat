package client;

import utils.ClientOptions;
import utils.DatagramPacketBuilder;
import utils.PORT;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socketSend;
    private DatagramSocket socketListen;
    private int listenPort;

    public UDPClient() {
        listenPort = randomlyPickPort(PORT.LOW_CLIENT_PORT, PORT.HIGH_CLIENT_PORT);
        try {
            serverAddress = InetAddress.getByName("localhost");
            socketSend = new DatagramSocket();
            socketListen = new DatagramSocket(listenPort);
        } catch (UnknownHostException | SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
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
        try {
            socketSend.send(DatagramPacketBuilder.build(login, serverAddress, PORT.SERVER_LOGIN));
        } catch (IOException ex) {
            LOGGER.severe("Unable to send initial message to server\t" + ex.getMessage());
        }
        sendListenPort(PORT.SERVER_LOGIN);
        try {
            var loginPacket = DatagramPacketBuilder.create();
            socketListen.receive(loginPacket);
            String loginMessage = DatagramPacketBuilder.toString(loginPacket);
            if (loginMessage.isEmpty()) {
                return false;
            } else {
                System.out.println(loginMessage);
                return true;
            }
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive login message from server\t" + ex.getMessage());
        }
        return false;
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
        try {
            socketSend.send(DatagramPacketBuilder.build(String.valueOf(listenPort), serverAddress, serverPort));
        } catch (IOException ex) {
            LOGGER.severe("Unable to send initial message to server\t" + ex.getMessage());
        }
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
