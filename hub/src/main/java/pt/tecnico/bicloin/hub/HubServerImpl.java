package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.domain.*;

import pt.tecnico.bicloin.hub.domain.exception.FailedPreconditionException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import static io.grpc.Status.FAILED_PRECONDITION;

import java.util.Map;

public class HubServerImpl extends HubServiceGrpc.HubServiceImplBase {
	private boolean DEBUG = false;

	/* Server Implementation */
	private Hub hub;

	public HubServerImpl(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations) {
		super();
		this.hub = new Hub(zooHost, zooPort, instance_num, users, stations);
	}
	
	public HubServerImpl(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations, boolean debug) {
		super();
		this.DEBUG = debug;
		this.hub = new Hub(zooHost, zooPort, instance_num, users, stations, DEBUG);
	}

	public void shutdown() {
		hub.shutdown();
	}

	public Hub getHub() { return hub; }


	@Override
	public void balance(BalanceRequest request, StreamObserver<AmountResponse> responseObserver) {
		String id = request.getUserId();

		try{
			AmountResponse response = hub.balance(id);
		
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);

		} catch (StatusRuntimeException e) {
			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
			debug("Got exception:" + e.getStatus().getDescription());
		}
		
	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<AmountResponse> responseObserver) {
		String id = request.getUserId();
		int value = request.getAmount();
		String phoneNumber = request.getPhoneNumber();

		try{
			AmountResponse response = hub.topUp(id, value, phoneNumber);
		
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);

		} catch (StatusRuntimeException e) {
			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
			debug("Got exception:" + e.getStatus().getDescription());
		} 
	}

	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		String stationId = request.getStationId();

		try{
			InfoStationResponse response = hub.infoStation(stationId);	

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("@HubServerImpl Got exception:" + e);
		} catch (StatusRuntimeException e) {
			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
			debug("@HubServerImpl Got exception:" + e.getStatus().getDescription());
		}
	}

	@Override
	public void bikeUp(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userId = request.getUserId();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();
		String stationId = request.getStationId();

		try{
			BikeResponse response = hub.bikeUp(userId, latitude, longitude, stationId);

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);

		} catch (FailedPreconditionException e) {
			responseObserver.onError(FAILED_PRECONDITION
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);
		} catch (StatusRuntimeException e) {
			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
			debug("Got exception:" + e.getStatus().getDescription());
		}
	}

	@Override
	public void bikeDown(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userId = request.getUserId();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();
		String stationId = request.getStationId();

		try{
			BikeResponse response = hub.bikeDown(userId, latitude, longitude, stationId);

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);

		} catch (FailedPreconditionException e) {
			responseObserver.onError(FAILED_PRECONDITION
				.withDescription(e.getMessage()).asRuntimeException());
			debug("Got exception:" + e);

		}
	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		int stations = request.getNStations();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();

		try{
			LocateStationResponse response = hub.locateStation(latitude, longitude, stations);	

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("@HubServerImpl Got exception:" + e);
		} catch (StatusRuntimeException e) {
			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
			debug("@HubServerImpl Got exception:" + e.getStatus().getDescription());
		}
	}

	@Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		String input = request.getInput();
		
		try{
			PingResponse response = hub.ping(input);	

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
			debug("@HubServerImpl Got exception:" + e);
		}
	}

	@Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		responseObserver.onNext(hub.getAllServerStatus());
		responseObserver.onCompleted();
	}


	/** Helper method to print debug messages. */
	private void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@HubServerImpl\t" +  debugMessage);
	}
    
}