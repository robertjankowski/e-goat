package client;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Specify IP as argument");
            System.exit(-1);
        }
        Client udpClient = new Client(args[0]);
        udpClient.runClient();
    }

}
