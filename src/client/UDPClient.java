package client;

import utils.ClientOptions;
import utils.Config;
import utils.User;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socketSend;
    private DatagramSocket socketListen;
    private DatagramSocket socketRequsts;

    private ExecutorService executor;

    public UDPClient() {
        try {
            serverAddress = InetAddress.getByName("localhost");
            socketSend = new DatagramSocket();
            socketListen = new DatagramSocket(Config.CLIENT_PORT_LISTEN);
            socketRequsts = new DatagramSocket(Config.CLIENT_PORT_SEND);
        } catch (UnknownHostException | SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        executor = Executors.newFixedThreadPool(1);
    }

    public void runClient() throws IOException {
        logging();
        while (true) {
            executor.submit(this::listen);
            var req = requests();
            if (req == ClientOptions.NONE || req == ClientOptions.EXIT) {
                System.exit(0);
            }
        }
    }

    private void logging() throws IOException {
        LOGGER.info("Server address: " + serverAddress);
        System.out.print("Login: ");
        var login = getMessage();
        var loginBytes = login.getBytes();
        socketSend.send(new DatagramPacket(loginBytes, loginBytes.length, serverAddress, Config.LOGIN_PORT));

        var greetingsPacket = Config.getDatagramPacket();
        socketListen.receive(greetingsPacket);
        var greetings = Config.datagramToString(greetingsPacket);
        if (greetings.isEmpty())
            LOGGER.log(Level.SEVERE, "Could not connect to server");
        System.out.println(greetings);
    }

    private void listen() {
        var choicePacket = Config.getDatagramPacket();
        try {
            socketListen.receive(choicePacket);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
        var choiceMessage = Config.datagramToString(choicePacket);
        switch (ClientOptions.valueOf(choiceMessage)) {
            case GET_LIST_OF_FILES:
                sendListOfFiles();
                break;
            case GET_FILE:
                System.out.println("GET_FILE");
                // connect via server to client with desired file
                break;
            case EXIT:
                LOGGER.log(Level.INFO, "Finished program");
                System.exit(0);
            default:
                // send error code
                break;
        }
    }

    private ClientOptions requests() {
        ClientOptions options = ClientOptions.NONE;
        while (options != ClientOptions.EXIT) {
            options = selectOption();
            var optionsBytes = options.toString().getBytes();
            try {
                socketSend.send(new DatagramPacket(optionsBytes, optionsBytes.length, serverAddress, Config.SERVER_PORT));

                var receivePacket = Config.getDatagramPacket();
                socketRequsts.receive(receivePacket);
                var mess = Config.datagramToString(receivePacket);
                System.out.println(mess);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return options;
    }

    private void sendListOfFiles() {
        var files = User.getFiles();
        var filesNumber = String.valueOf(files.size()).getBytes();
        try {
            socketSend.send(new DatagramPacket(filesNumber, filesNumber.length, serverAddress, Config.SERVER_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Config.wait_ms(10);
        for (var file : files) {
            var fileBytes = file.getBytes();
            try {
                socketSend.send(new DatagramPacket(fileBytes, fileBytes.length, serverAddress, Config.SERVER_PORT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
