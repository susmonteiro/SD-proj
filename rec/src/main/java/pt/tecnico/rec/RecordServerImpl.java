package pt.tecnico.rec;


import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.domain.*;

import static io.grpc.Status.INVALID_ARGUMENT;

public class RecordServerImpl extends RecordServiceGrpc.RecordServiceImplBase {

	private final Rec rec = new Rec();

	@Override
    public void read(RegisterRequest request, StreamObserver<ReadResponse> responseObserver) {
		String id = request.getId();
		RegisterValue.ValueCase type = request.getData().getValueCase();

		RegisterValue value = rec.getRegister(id, type);

		ReadResponse response = ReadResponse.newBuilder().setData(value).build();
		
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		
	}

	@Override
    public void write(RegisterRequest request, StreamObserver<WriteResponse> responseObserver) {
		String id = request.getId();
		RegisterValue value = request.getData();
		RegisterValue.ValueCase type = value.getValueCase();

		rec.setRegister(id, type, value);

		WriteResponse response = WriteResponse.getDefaultInstance();
		
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

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