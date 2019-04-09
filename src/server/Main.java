package server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        UDPServer udpServer;
        try {
            udpServer = UDPServer.getUdpServer();
            udpServer.runServer();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }
}
