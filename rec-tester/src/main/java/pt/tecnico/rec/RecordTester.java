package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;

import pt.tecnico.rec.frontend.RecordFrontend;

public class RecordTester {
	
	private static final String registerIdDefault = "rec-tester";

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
		// pingTest(getPingRequest("friend"));
		// pingTest(getPingRequest(""));

		// /* Write */
		writeTest(registerIdDefault, getRegisterBalanceAsRegisterValue(1));
		// writeTest(registerIdDefault, getRegisterOnBikeAsRegisterValue(true));
		// writeTest(registerIdDefault, getRegisterNBikesAsRegisterValue(2));
		// writeTest(registerIdDefault, getRegisterNPickUpsAsRegisterValue(3));
		// writeTest(registerIdDefault, getRegisterNDeliveriesAsRegisterValue(4));

		/* Read */
		readTest(registerIdDefault, getRegisterBalanceAsRegisterValue());
		// readTest(registerIdDefault, getRegisterOnBikeAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNBikesAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNPickUpsAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNDeliveriesAsRegisterValue());

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

	private static void writeTest(String registerId, RegisterValue value) {
		try{
			RegisterRequest request = RegisterRequest.newBuilder()
				.setId(registerId)
				.setData(value)
				.build();
			WriteResponse response = frontend.write(request);
			System.out.println("@WriteTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@WriteTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}

	private static void readTest(String registerId, RegisterValue value) {
		try{
			RegisterRequest request = RegisterRequest.newBuilder()
				.setId(registerId)
				.setData(value)
				.build();
			ReadResponse response = frontend.read(request);
			System.out.println("@ReadTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@ReadTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}


	/* ================ */
	/* Message building */

	private static PingRequest getPingRequest(String input) {
		return PingRequest.newBuilder().setInput(input).build();
	}

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

}