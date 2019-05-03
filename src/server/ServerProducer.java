package server;

import datagram.DatagramPacketBuilder;
import message.Message;
import user.User;

import java.util.concurrent.ArrayBlockingQueue;

public class ServerProducer extends Server {

    public ServerProducer(ArrayBlockingQueue<Message> eventsQueue, int port) {
        super(eventsQueue, port);
    }

    public void run() {
        var packet = socket.receive();
        var user = new User(packet.getAddress());
        var event = new Message(DatagramPacketBuilder.toString(packet), user);
        try {
            eventsQueue.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
