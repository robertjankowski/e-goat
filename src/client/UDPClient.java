package client;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import message.Message;
import utils.ClientOptions;
import utils.PORT;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());

    private InetAddress serverAddress;
    private UDPSocket socketSend;
    private UDPSocket socketListenPort1;
    private UDPSocket socketListenPort2;

    private int randomPort1;
    private int randomPort2;

    public UDPClient() {
        socketSend = new UDPSocket();
        try {
            serverAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {
            LOGGER.severe("Unable to establish server address");
        }
    }

    public void runClient() {
        login();
        while (true) {
            // second thread for listening on randomPort2
            if (selectOption() == ClientOptions.EXIT)
                break;
        }
    }

    private void login() {
        socketSend.send(Message.LOGIN, serverAddress, PORT.SERVER_PRODUCER);
        receiveRandomPorts();
        System.out.print("Login: ");
        var login = getMessage();
        socketSend.send(login, serverAddress, PORT.SERVER_CONSUMER);
        System.out.println(receiveWelcomeMessage());
    }

    private void receiveRandomPorts() {
        var socket = new UDPSocket(PORT.INIT_CLIENT);
        randomPort1 = DatagramPacketBuilder.toInt(socket.receive());
        randomPort2 = DatagramPacketBuilder.toInt(socket.receive());
        socket.close();
        socketListenPort1 = new UDPSocket(randomPort1);
        socketListenPort2 = new UDPSocket(randomPort2);
    }

    private String receiveWelcomeMessage() {
        var message = socketListenPort1.receive();
        return DatagramPacketBuilder.toString(message);
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
