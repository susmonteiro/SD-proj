package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.stub.StreamObserver;

import static io.grpc.Status.INVALID_ARGUMENT;

public class HubServerImpl extends HubServiceGrpc.HubServiceImplBase {

	@Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		String input = request.getInput();
		
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription("Input cannot be empty!").asRuntimeException());	

		} else {
			String output = "Hello " + input + "! " + HubMain.identity();
			PingResponse response = PingResponse.newBuilder().setOutput(output).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			
		}
	}
    
}