package server;

import message.Message;
import utils.PORT;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    private ServerProducer serverProducer;
    private ServerConsumer serverConsumer;
    private ExecutorService executor;
    private static final int MAX_CAPACITY = 10;

    public MainServer(String ip) {
        var eventsQueue = new ArrayBlockingQueue<Message>(MAX_CAPACITY);
        serverProducer = new ServerProducer(eventsQueue, PORT.SERVER_PRODUCER, ip);
        serverConsumer = new ServerConsumer(eventsQueue, PORT.SERVER_CONSUMER, ip);
        executor = Executors.newSingleThreadExecutor();
    }

    public void runServer() {
        while (true) {
            serverProducer.run();
            executor.submit(serverConsumer::run);
        }
    }

    public static void main(String[] args) {
        MainServer server = new MainServer(args[0]);
        server.runServer();
    }
}
