package pt.tecnico.bicloin.hub.frontend;

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

	public AmountResponse balance(BalanceRequest request) {
		return stub.balance(request);
	}

	public AmountResponse topUp(TopUpRequest request) {
		return stub.topUp(request);
	}

	public InfoStationResponse infoStation(InfoStationRequest request) {
		return stub.infoStation(request);
	}

	public LocateStationResponse locateStation(LocateStationRequest request) {
		return stub.locateStation(request);
	}

	public BikeResponse bikeUp(BikeRequest request) {
		return stub.bikeUp(request);
	}

	public BikeResponse bikeDown(BikeRequest request) {
		return stub.bikeDown(request);
	}

	public PingResponse ping(PingRequest request) {
		return stub.ping(request);
	}

	public SysStatusResponse sysStatus(SysStatusRequest request) {
		return stub.sysStatus(request);
	}
	
    @Override
	public final void close() {
		channel.shutdown();
	}
}