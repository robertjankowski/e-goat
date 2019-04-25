package server;

import events.Event;

import java.util.concurrent.ArrayBlockingQueue;

public class ServerConsumer extends Server {

    public ServerConsumer(ArrayBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
    }
}
