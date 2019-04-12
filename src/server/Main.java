package server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        UDPServer udpServer = UDPServer.getInstance();
        try {
            udpServer.runServer();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

    }
}
