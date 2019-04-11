package server;

import utils.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    private static UDPServer udpServer = null;

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    private UDPServer() throws SocketException {
        datagramSocket = new DatagramSocket(Config.PORT);
        datagramPacket = getDatagramPacket();
    }

    public static UDPServer getUdpServer() throws SocketException {
        if (udpServer == null) {
            udpServer = new UDPServer();
        }
        return udpServer;
    }

    public void runServer() throws IOException {

        while (true) {
            datagramPacket = getDatagramPacket();
            datagramSocket.receive(datagramPacket);
            String filename = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);

            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file);

            while(true){
                datagramPacket = getDatagramPacket();
                datagramSocket.receive(datagramPacket);
                ByteBuffer bf = ByteBuffer.wrap(datagramPacket.getData());
                int length = bf.getInt();

                if(length == 0)
                    break;

                datagramPacket = getDatagramPacket();
                datagramSocket.receive(datagramPacket);
                byte[] byteArray = datagramPacket.getData();
                fos.write(byteArray, 0, length);
            }

            fos.flush();
            fos.close();
        }

    }

    private DatagramPacket getDatagramPacket() {
        return new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
    }
}
