package pt.tecnico.rec;

import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.stub.StreamObserver;

public class RecordServerImpl extends RecordServiceGrpc.RecordServiceImplBase {

	@Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		String input = request.getInput();
		String output = "Hello " + input + "!";
		PingResponse response = PingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
    
}