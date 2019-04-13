package server;

public class Main {

    public static void main(String[] args) {
        UDPServer udpServer = UDPServer.getInstance();
        udpServer.runServer();
    }
}
