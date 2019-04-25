package server;

import events.Event;

import java.util.concurrent.ArrayBlockingQueue;

public class ServerProducer extends Server {

    public ServerProducer(ArrayBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
    }

}
