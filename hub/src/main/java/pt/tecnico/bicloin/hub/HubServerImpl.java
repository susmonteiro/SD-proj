package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.Debug;
import pt.tecnico.bicloin.hub.domain.*;

import pt.tecnico.bicloin.hub.domain.exception.FailedPreconditionException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import static io.grpc.Status.FAILED_PRECONDITION;

import static pt.tecnico.bicloin.hub.HubMain.debugDemo;

import java.util.Map;

public class HubServerImpl extends HubServiceGrpc.HubServiceImplBase {
	private Debug DEBUG = Debug.NO_DEBUG;

	/* Server Implementation */
	private Hub hub;

	public HubServerImpl(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations) {
		super();
		this.hub = new Hub(zooHost, zooPort, instance_num, users, stations);
	}
	
	public HubServerImpl(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations, Debug debug) {
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
			debugDemo("===\tBALANCE REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			AmountResponse response = hub.balance(id);
		
			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());

		} catch (StatusRuntimeException e) {
			debugDemo("++ Sending to App exception UNAVAILABLE - " + e.getMessage() + "\n");

			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
		}
		
	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<AmountResponse> responseObserver) {
		String id = request.getUserId();
		int value = request.getAmount();
		String phoneNumber = request.getPhoneNumber();

		try{
			debugDemo("===\tTOP UP REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			AmountResponse response = hub.topUp(id, value, phoneNumber);

			debugDemo("++ Sending to App: \n" + response);
		
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());

		} catch (StatusRuntimeException e) {
			debugDemo("++ Sending to App exception UNAVAILABLE - " + e.getMessage() + "\n");

			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
		} 
	}

	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		String stationId = request.getStationId();

		try{
			debugDemo("===\tINFO STATION REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			InfoStationResponse response = hub.infoStation(stationId);	

			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
		} catch (StatusRuntimeException e) {
			debugDemo("++ Sending to App exception UNAVAILABLE - " + e.getMessage() + "\n");

			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
		}
	}

	@Override
	public void bikeUp(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userId = request.getUserId();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();
		String stationId = request.getStationId();

		try{
			debugDemo("===\tBIKE UP REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			BikeResponse response = hub.bikeUp(userId, latitude, longitude, stationId);
			
			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());

		} catch (FailedPreconditionException e) {
			debugDemo("++ Sending to App exception FAILED_PRECONDITION - " + e.getMessage() + "\n");

			responseObserver.onError(FAILED_PRECONDITION
				.withDescription(e.getMessage()).asRuntimeException());
		} catch (StatusRuntimeException e) {
			debugDemo("++ Sending to App exception UNAVAILABLE - " + e.getMessage() + "\n");

			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
		}
	}

	@Override
	public void bikeDown(BikeRequest request, StreamObserver<BikeResponse> responseObserver) {
		String userId = request.getUserId();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();
		String stationId = request.getStationId();

		try{
			debugDemo("===\tBIKE DOWN REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			BikeResponse response = hub.bikeDown(userId, latitude, longitude, stationId);

			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());

		} catch (FailedPreconditionException e) {
			debugDemo("++ Sending to App exception FAILED_PRECONDITION - " + e.getMessage() + "\n");

			responseObserver.onError(FAILED_PRECONDITION
				.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		int stations = request.getNStations();
		float latitude = request.getCoordinates().getLatitude();
		float longitude = request.getCoordinates().getLongitude();

		try{
			debugDemo("===\tLOCATE STATION REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			LocateStationResponse response = hub.locateStation(latitude, longitude, stations);
			
			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
		} catch (StatusRuntimeException e) {
			debugDemo("++ Sending to App exception UNAVAILABLE - " + e.getMessage() + "\n");

			responseObserver.onError(UNAVAILABLE
				.withDescription("Request could not be processed.").asRuntimeException());
		}
	}

	@Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		String input = request.getInput();
		
		try{
			debugDemo("===\tPING REQUEST\t===");
			debugDemo("++ Received from App:\n" + request);

			PingResponse response = hub.ping(input);	

			debugDemo("++ Sending to App: \n" + response);

			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidArgumentException e) {
			debugDemo("++ Sending to App exception INVALID_ARGUMENT - " + e.getMessage() + "\n");

			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		debugDemo("===\tSYSTEM STATUS REQUEST\t===");
		debugDemo("++ Received from App:\n" + request);

		responseObserver.onNext(hub.getAllServerStatus());
		responseObserver.onCompleted();
	}


	/** Helper method to print debug messages. */
	public void debug(Object debugMessage) {
		if (DEBUG == Debug.STRONGER_DEBUG)
			System.err.println("@HubServerImpl\t" +  debugMessage);
	}
    
}