package client;

import utils.ClientOptions;
import utils.DatagramPacketBuilder;
import utils.Message;
import utils.PORT;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socketSend;
    private DatagramSocket socketInitListen;
    private DatagramSocket socketListen;

    public UDPClient() {
        try {
            serverAddress = InetAddress.getByName("localhost");
            socketSend = new DatagramSocket();
            socketInitListen = new DatagramSocket(PORT.INIT_CLIENT);
        } catch (UnknownHostException | SocketException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    public void runClient() throws IOException {
        int port = askForPort();
        if (port < 0)
            LOGGER.log(Level.WARNING, "Could not connect to server");
        socketListen = new DatagramSocket(port);
        LOGGER.info("Client listening on port: " + port);
        while (true) {
            // logging
        }
    }

    private int askForPort() {
        try {
            socketSend.send(DatagramPacketBuilder.build(Message.INIT_PORT, serverAddress, PORT.SERVER));
        } catch (IOException ex) {
            LOGGER.severe("Unable to send initial message to server\t" + ex.getMessage());
        }
        try {
            var initialPacket = DatagramPacketBuilder.create();
            socketInitListen.receive(initialPacket);
            int port = DatagramPacketBuilder.toInt(initialPacket);
            return port > 0 ? port : -1;
        } catch (IOException ex) {
            LOGGER.severe("Unable to receive initial message to server\t" + ex.getMessage());
        }
        return -1;
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
