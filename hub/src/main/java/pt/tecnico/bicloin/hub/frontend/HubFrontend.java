package pt.tecnico.bicloin.hub.frontend;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import io.grpc.StatusRuntimeException;


public class HubFrontend extends MessageHelper implements AutoCloseable {
	private boolean DEBUG;

    private final ManagedChannel channel;
	private final HubServiceGrpc.HubServiceBlockingStub stub;

	public HubFrontend(String host, int port) {
		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

		// Create a blocking stub.
		stub = HubServiceGrpc.newBlockingStub(channel);
	}

	public HubFrontend(String host, int port, boolean debug) {
		this(host, port);
		DEBUG = debug;
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


	/* Hub Getters and Setters */
    /* ========================== */

    public AmountResponse doBalanceOperation(String id) throws StatusRuntimeException {
        BalanceRequest request = getBalanceRequest(id);
        debug("#doBalanceOperation\n**Request:\n" + request);
        
        AmountResponse response = balance(request);
        debug("#doBalanceOperation\n**Response:\n" + response);

        return response;
    }

    public AmountResponse doTopUpOperation(String id, int value, String phoneNumber) throws StatusRuntimeException {
        TopUpRequest request = getTopUpRequest(id, value, phoneNumber);
        debug("#doTopUpOperation\n**Request:\n" + request);

		AmountResponse response = topUp(request);
        debug("#doTopUpOperation\n**Response:\n" + response);

        return response;
    }

	public InfoStationResponse doInfoStationOperation(String id) throws StatusRuntimeException {
        InfoStationRequest request = getInfoStationRequest(id);
        debug("#doInfoStationOperation\n**Request:\n" + request);

		InfoStationResponse response = infoStation(request);
        debug("#doInfoStationOperation\n**Response:\n" + response);

        return response;
    }

	public LocateStationResponse doLocateStationOperation(float latitude, float longitude, int nStations) throws StatusRuntimeException {
        LocateStationRequest request = getLocateStationRequest(latitude, longitude, nStations);
        debug("#doLocateStationOperation\n**Request:\n" + request);

		LocateStationResponse response = locateStation(request);
        debug("#doLocateStationOperation\n**Response:\n" + response);

        return response;
    }

	public void doBikeUpOperation(String userId, float latitude, float longitude, String stationId) throws StatusRuntimeException {
        BikeRequest request = getBikeRequest(userId, latitude, longitude, stationId);
        debug("#doBikeUpOperation\n**Request:\n" + request);

		bikeUp(request);
    }

	public void doBikeDownOperation(String userId, float latitude, float longitude, String stationId) throws StatusRuntimeException {
        BikeRequest request = getBikeRequest(userId, latitude, longitude, stationId);
        debug("#doBikeDownOperation\n**Request:\n" + request);

		bikeDown(request);
    }

	public PingResponse doPingOperation(String input) throws StatusRuntimeException {
        PingRequest request = getPingRequest(input);
        debug("#doPingOperation\n**Request:\n" + request);

		PingResponse response = ping(request);
        debug("#doPingOperation\n**Response:\n" + response);

        return response;
    }
	
	public SysStatusResponse doSysStatusOperation() throws StatusRuntimeException {
        SysStatusRequest request = getSysStatusRequest();
        debug("#doSysStatusOperation\n**Request:\n" + request);

		return sysStatus(request);
    }


	/** Helper method to print debug messages. */
	private void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println(debugMessage);
	}
}