package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class HubFrontend implements AutoCloseable {
    private final ManagedChannel channel;
	private final HubServiceGrpc.HubServiceBlockingStub stub;

	public HubFrontend(String host, int port) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = HubServiceGrpc.newBlockingStub(channel);
	}

	public PingResponse ping(PingRequest request) {
		return stub.ping(request);
	}
	
    @Override
	public final void close() {
		channel.shutdown();
	}
}