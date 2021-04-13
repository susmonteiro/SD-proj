package pt.tecnico.rec;

import pt.tecnico.rec.frontend.RecordFrontend; 
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;

public class RecordTester {
	
	private static final RegisterRequest registerDefault = RegisterRequest.newBuilder()
		.setId("rec-tester").build();

	private static RecordFrontend frontend;

	public static void main(String[] args) {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", RecordTester.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		frontend = new RecordFrontend(host, port);
		
		/* Ping */
		pingTest(getPingRequest("friend"));
		pingTest(getPingRequest(""));

		/* Write */
		writeTest(registerDefault, getRegisterBalanceAsRegisterValue(1));
		writeTest(registerDefault, getRegisterOnBikeAsRegisterValue(true));
		writeTest(registerDefault, getRegisterNBikesAsRegisterValue(2));
		writeTest(registerDefault, getRegisterNPickUpsAsRegisterValue(3));
		writeTest(registerDefault, getRegisterNDeliveriesAsRegisterValue(4));

		/* Read */
		readTest(registerDefault, getRegisterBalanceAsRegisterValue());
		readTest(registerDefault, getRegisterOnBikeAsRegisterValue());
		readTest(registerDefault, getRegisterNBikesAsRegisterValue());
		readTest(registerDefault, getRegisterNPickUpsAsRegisterValue());
		readTest(registerDefault, getRegisterNDeliveriesAsRegisterValue());

		frontend.close();
	}
	
	/* ============== */
	/* Method testing */

	private static void pingTest(PingRequest request) {
		try{
			PingResponse response = frontend.ping(request);
			System.out.println("@PingTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@PingTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}

	private static void writeTest(RegisterRequest register, RegisterValue value) {
		try{
			WriteRequest request = WriteRequest.newBuilder()
				.setRegister(registerDefault)
				.setData(value)
				.build();
			WriteResponse response = frontend.write(request);
			System.out.println("@WriteTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@WriteTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}

	private static void readTest(RegisterRequest register, RegisterValue value) {
		try{
			ReadRequest request = ReadRequest.newBuilder()
				.setRegister(registerDefault)
				// .setData(value)
				.build();
			// ReadResponse response = frontend.read(request);
			// System.out.println("@ReadTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@ReadTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}


	/* ================ */
	/* Message building */

	private static RegisterValue getRegisterBalanceAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegBalance(
				RegisterBalance.newBuilder().setBalance(value).build()
			).build();
	}
	private static RegisterValue getRegisterBalanceAsRegisterValue() {
		return RegisterValue.newBuilder().setRegBalance(
				RegisterBalance.getDefaultInstance()
			).build();
	}

	private static RegisterValue getRegisterOnBikeAsRegisterValue(boolean value) {
		return RegisterValue.newBuilder().setRegOnBike(
				RegisterOnBike.newBuilder().setOnBike(value).build()
			).build();
	}
	private static RegisterValue getRegisterOnBikeAsRegisterValue() {
		return RegisterValue.newBuilder().setRegOnBike(
				RegisterOnBike.getDefaultInstance()
			).build();
	}
	
	private static RegisterValue getRegisterNBikesAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNBikes(
				RegisterNBikes.newBuilder().setNBikes(value).build()
			).build();
	}
	private static RegisterValue getRegisterNBikesAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNBikes(
				RegisterNBikes.getDefaultInstance()
			).build();
	}

	private static RegisterValue getRegisterNPickUpsAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNPickUps(
				RegisterNPickUps.newBuilder().setNPickUps(value).build()
			).build();
	}
	private static RegisterValue getRegisterNPickUpsAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNPickUps(
				RegisterNPickUps.getDefaultInstance()
			).build();
	}

	private static RegisterValue getRegisterNDeliveriesAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNDeliveries(
				RegisterNDeliveries.newBuilder().setNDeliveries(value).build()
			).build();
	}
	private static RegisterValue getRegisterNDeliveriesAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNDeliveries(
				RegisterNDeliveries.getDefaultInstance()
			).build();
	}

	private static PingRequest getPingRequest(String input) {
		return PingRequest.newBuilder().setInput(input).build();
	}
}