package server;

import datagram.UDPSocket;
import message.Message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class Server {

    protected ArrayBlockingQueue<Message> eventsQueue;
    protected UDPSocket socket;

    public Server(ArrayBlockingQueue<Message> eventsQueue, int port, String ip) {
        this.eventsQueue = eventsQueue;
        try {
            socket = new UDPSocket(port, InetAddress.getByName(ip));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
