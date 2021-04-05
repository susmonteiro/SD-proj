package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;

public class RecordFrontend implements AutoCloseable {
	private final ManagedChannel channel;
	private final RecordServiceGrpc.RecordServiceBlockingStub stub;

	public RecordFrontend(String host, int port) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = RecordServiceGrpc.newBlockingStub(channel);
	}

	public PingResponse ping(PingRequest request) {
		return stub.ping(request);
	}
	
    @Override
	public final void close() {
		channel.shutdown();
	}
}