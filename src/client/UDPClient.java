package client;

import utils.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UDPClient {

    private static Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private InetAddress serverAddress;
    private DatagramSocket socket;


    public UDPClient() throws UnknownHostException, SocketException {
        serverAddress = InetAddress.getByName("localhost");
        socket = new DatagramSocket();
    }

    private String getMessage() {
        var input = new Scanner(System.in);
        return input.next();
    }

    public void runClient(boolean once) throws IOException {
        LOGGER.info("Server address: " + serverAddress);
        do {
            System.out.print("Path to file: ");
            var path = getMessage();
            FileInputStream fis = new FileInputStream(path);

            byte[] byteName = path.getBytes();
            socket.send(new DatagramPacket(byteName, byteName.length, serverAddress, Config.PORT));
            wait_ms();

            int count;
            byte[] byteArray = new byte[Config.BUFFER_SIZE];
            while((count = fis.read(byteArray)) != -1){
                byte[] lengthBytes = ByteBuffer.allocate(4).putInt(count).array();
                socket.send(new DatagramPacket(lengthBytes, 4, serverAddress, Config.PORT));
                wait_ms();

                socket.send(new DatagramPacket(byteArray, count, serverAddress, Config.PORT));
                wait_ms();
            }
            fis.close();

            byte[] lengthBytes = {0};
            socket.send(new DatagramPacket(lengthBytes, 1, serverAddress, Config.PORT));

        } while (!once);

    }

    private void wait_ms(){
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
