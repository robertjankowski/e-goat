package client;

public class Main2 {

    public static void main(String[] args) {
        Client udpClient = new Client(args[0]);
        udpClient.runClient();
    }
}
