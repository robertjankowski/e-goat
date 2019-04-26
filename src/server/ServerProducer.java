package server;

import datagram.DatagramPacketBuilder;
import datagram.UDPSocket;
import message.Message;
import user.User;
import utils.PORT;

import java.util.concurrent.ArrayBlockingQueue;

public class ServerProducer extends Server {

    private UDPSocket socket;

    public ServerProducer(ArrayBlockingQueue<Message> eventsQueue) {
        super(eventsQueue);
        socket = new UDPSocket(PORT.SERVER_PRODUCER);
    }

    public void run() {
        var packet = socket.receive();
        var user = new User(packet.getAddress());
        var event = new Message(DatagramPacketBuilder.toString(packet), user);
        eventsQueue.add(event);
    }

}
