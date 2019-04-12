package client;

import utils.ClientOptions;
import utils.Config;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socketSend;
    private DatagramSocket socketListen;

    public UDPClient() {
        try {
            serverAddress = InetAddress.getByName("localhost");
            socketSend = new DatagramSocket();
            socketListen = new DatagramSocket(Config.CLIENT_PORT_LISTEN);
        } catch (UnknownHostException | SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    public void runClient() throws IOException {
        LOGGER.info("Server address: " + serverAddress);
        System.out.print("Login: ");
        var login = getMessage();
        var loginBytes = login.getBytes();
        socketSend.send(new DatagramPacket(loginBytes, loginBytes.length, serverAddress, Config.LOGIN_PORT));

        var greetingsPacket = Config.getDatagramPacket();
        socketListen.receive(greetingsPacket);
        var greetings = new String(greetingsPacket.getData(), 0, greetingsPacket.getLength(), StandardCharsets.UTF_8);
        if (greetings.isEmpty())
            LOGGER.log(Level.SEVERE, "Could not connect to server");
        System.out.println(greetings);

        ClientOptions options = ClientOptions.NONE;
        while (options != ClientOptions.EXIT) {
            options = selectOption();
            switch (options) {
                case GET_LIST_OF_FILES:
                    // receive from server files from others clients
                    break;
                case GET_FILE:
                    // connect via server to client with desired file
                    break;
                default:
                    break;
            }
        }
    }

    private String getMessage() {
        var input = new Scanner(System.in);
        return input.next();
    }

    private void wait_ms() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ClientOptions selectOption() {
        printOptions();
        int choice = Integer.valueOf(getMessage());
        return ClientOptions.fromId(choice);
    }

    private void printOptions() {
        System.out.println("\n\tOPTIONS");
        System.out.println("\t1. Get list of available files");
        System.out.println("\t2. Get file");
        System.out.println("\t3. Exit\n");
    }
}
