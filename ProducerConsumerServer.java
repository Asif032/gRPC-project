import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import producerconsumer.ProducerConsumerGrpc;
import producerconsumer.Data;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerServer {
    private final int port;
    private final BlockingQueue<Data> queue = new LinkedBlockingQueue<>();

    public ProducerConsumerServer(int port) {
        this.port = port;
    }

    private void start() throws IOException {
        Server server = ServerBuilder.forPort(port)
                .addService(new ProducerConsumerImpl())
                .build()
                .start();

        System.out.println("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.shutdown();
        }));

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ProducerConsumerImpl extends ProducerConsumerGrpc.ProducerConsumerImplBase {
        @Override
        public void produce(Data request, StreamObserver<Data> responseObserver) {
            try {
                queue.put(request);
                System.out.println("Produced: " + request.getValue());
                responseObserver.onNext(Data.newBuilder().setValue("Produced: " + request.getValue()).build());
                responseObserver.onCompleted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void consume(Data request, StreamObserver<Data> responseObserver) {
            try {
                Data data = queue.take();
                System.out.println("Consumed: " + data.getValue());
                responseObserver.onNext(data);
                responseObserver.onCompleted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 50051;
        ProducerConsumerServer server = new ProducerConsumerServer(port);
        server.start();
    }
}
