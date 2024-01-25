import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import producerconsumer.ProducerConsumerGrpc;
import producerconsumer.Data;

public class ProducerConsumerClient {
    private final ManagedChannel channel;
    private final ProducerConsumerGrpc.ProducerConsumerStub asyncStub;

    public ProducerConsumerClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        asyncStub = ProducerConsumerGrpc.newStub(channel);
    }

    public void produce(String value) {
        Data request = Data.newBuilder().setValue(value).build();
        asyncStub.produce(request, new StreamObserver<Data>() {
            @Override
            public void onNext(Data value) {
                System.out.println("Server response: " + value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Production completed");
                channel.shutdown();
            }
        });
    }

    public void consume() {
        Data request = Data.newBuilder().build();
        asyncStub.consume(request, new StreamObserver<Data>() {
            @Override
            public void onNext(Data value) {
                System.out.println("Server response: " + value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("Consumption completed");
                channel.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        ProducerConsumerClient client = new ProducerConsumerClient("localhost", 50051);

        // Produce
        client.produce("Data 1");

        // Consume
        client.consume();
    }
}
