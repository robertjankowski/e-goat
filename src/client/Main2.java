package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main2 {

    private static Logger LOGGER = Logger.getLogger(Main2.class.getName());

    public static void main(String[] args) {

        UDPClient udpClient = new UDPClient();
        try {
            udpClient.runClient();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
}
