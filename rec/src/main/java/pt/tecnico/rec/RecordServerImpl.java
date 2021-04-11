package pt.tecnico.rec;


import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.stub.StreamObserver;

import static io.grpc.Status.INVALID_ARGUMENT;

public class RecordServerImpl extends RecordServiceGrpc.RecordServiceImplBase {

	@Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		String input = request.getInput();
		
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription("Input cannot be empty!").asRuntimeException());	

		} else {
			String output = "Hello " + input + "! " + RecordMain.identity();
			PingResponse response = PingResponse.newBuilder().setOutput(output).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			
		}
	}
    
}