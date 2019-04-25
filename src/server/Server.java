package server;

import events.Event;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class Server {
    ArrayBlockingQueue<Event> eventQueue;
}
