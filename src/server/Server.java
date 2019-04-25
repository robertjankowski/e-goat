package server;

import message.Message;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class Server {

    public static final int MAX_CAPACITY = 1024;
    protected ArrayBlockingQueue<Message> eventsQueue;

    public Server(ArrayBlockingQueue<Message> eventsQueue) {
        this.eventsQueue = eventsQueue;
    }
}
