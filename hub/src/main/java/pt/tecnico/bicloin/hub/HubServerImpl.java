package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.stub.StreamObserver;
import static pt.tecnico.bicloin.hub.HubMain.debug;
import pt.tecnico.bicloin.hub.domain.*;

import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;

import java.util.Map;

public class HubServerImpl extends HubServiceGrpc.HubServiceImplBase {

	/* Server Implementation */
	private Hub hub;

	public HubServerImpl(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations) {
		super();
		this.hub = new Hub(recIP, recPORT, users, stations);
	}

	public Hub getHub() { return hub; }


	@Override
	public void balance(BalanceRequest request, StreamObserver<AmountResponse> responseObserver) {
		String id = request.getUserId();

		try{
			int value = hub.balance(id);
			AmountResponse response = AmountResponse.newBuilder()
				.setBalance(value)
				.build();
		
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
	public void topUp(TopUpRequest request, StreamObserver<AmountResponse> responseObserver) {
		String id = request.getUserId();
		int value = request.getAmount();
		String phoneNumber = request.getPhoneNumber();

		try{
			int newValue = hub.topUp(id, value, phoneNumber);
			AmountResponse response = AmountResponse.newBuilder()
				.setBalance(newValue)
				.build();
		
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
		
		// Checks
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription("Input cannot be empty.").asRuntimeException());	
			return;
		}

		// Response
		String output = "Hello " + input + "! " + HubMain.identity();
		PingResponse response = PingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		responseObserver.onNext(hub.getAllServerStatus());
		responseObserver.onCompleted();
	}
    
}