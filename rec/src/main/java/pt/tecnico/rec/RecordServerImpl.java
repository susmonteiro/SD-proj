package pt.tecnico.rec;


import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.domain.*;
import pt.tecnico.rec.domain.exception.InvalidArgumentException;

import static io.grpc.Status.INVALID_ARGUMENT;

import static pt.tecnico.rec.RecordMain.debugDemo;


public class RecordServerImpl extends RecordServiceGrpc.RecordServiceImplBase {

	private final Rec rec = new Rec();

	@Override
    public void read(RegisterRequest request, StreamObserver<ReadResponse> responseObserver) {
		String id = request.getId();
		RegisterValue.ValueCase type = request.getData().getValue().getValueCase();
		try {
			debugDemo("===\tREAD REQUEST\t===");
			debugDemo("> Received " + type + " read request");

			RegisterData data = rec.getRegister(id, type);

			ReadResponse response = ReadResponse.newBuilder().setData(data).build();
			
			debugDemo("> Sending data\n" + data.getValue() + "\n");

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("> Sending exception INVALID_ARGUMENT - " + e.getMessage() + "\n");
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
    public void write(RegisterRequest request, StreamObserver<WriteResponse> responseObserver) {
		String id = request.getId();
		RegisterData data = request.getData();
		RegisterValue.ValueCase type = data.getValue().getValueCase();

		try {
			debugDemo("===\tWRITE REQUEST\t===");
			debugDemo("> Received data\n" + data.getValue());

			rec.setRegister(id, type, data);

			WriteResponse response = WriteResponse.getDefaultInstance();

			debugDemo("> Sending write ACK...\n");

			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InvalidArgumentException e) {
			debugDemo("> Sending exception INVALID_ARGUMENT - " + e.getMessage() + "\n");
			responseObserver.onError(INVALID_ARGUMENT
				.withDescription(e.getMessage()).asRuntimeException());
		}	
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