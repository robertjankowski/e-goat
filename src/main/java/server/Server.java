package server;

import datagram.UDPSocket;
import message.Message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class Server {

    protected static final int MAX_CAPACITY = 10;
    protected ArrayBlockingQueue<Message> eventsQueue;
    protected UDPSocket socket;

    public Server(ArrayBlockingQueue<Message> eventsQueue, int port) {
        this.eventsQueue = eventsQueue;
        try {
            socket = new UDPSocket(port, InetAddress.getByName("192.168.0.24"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
