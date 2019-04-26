package server;

import message.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    private ServerProducer serverProducer;
    private ServerConsumer serverConsumer;
    private ExecutorService executor;

    public MainServer() {
        var eventsQueue = new ArrayBlockingQueue<Message>(Server.MAX_CAPACITY);
        serverProducer = new ServerProducer(eventsQueue);
        serverConsumer = new ServerConsumer(eventsQueue);
        executor = Executors.newSingleThreadExecutor();
    }

    public void runServer() {
        while (true) {
            serverProducer.run();
            executor.submit(serverConsumer::run);
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.runServer();
    }
}
