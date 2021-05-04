package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;

import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;
import static pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordTester {
	
	private static final String registerIdDefault = "rec-tester";

	private static RecordFrontendReplicationWrapper frontend;

	public static void main(String[] args) throws ZKNamingException{
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 3) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", RecordTester.class.getName());
			return;
		}

		final String zooHost = args[0];
		final int zooPort = Integer.parseInt(args[1]);

		frontend = new RecordFrontendReplicationWrapper(zooHost, zooPort, 1, true);
		

		/* Ping */
		pingTest("friend");
		pingTest("");

		/* Write */
		// writeTest(registerIdDefault, getRegisterBalanceAsRegisterValue(0));
		// writeTest(registerIdDefault, getRegisterOnBikeAsRegisterValue(true));
		// writeTest(registerIdDefault, getRegisterNBikesAsRegisterValue(2));
		// writeTest(registerIdDefault, getRegisterNPickUpsAsRegisterValue(3));
		// writeTest(registerIdDefault, getRegisterNDeliveriesAsRegisterValue(4));

		/* Read */
		// readTest(registerIdDefault, getRegisterBalanceAsRegisterValue());
		// readTest(registerIdDefault, getRegisterOnBikeAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNBikesAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNPickUpsAsRegisterValue());
		// readTest(registerIdDefault, getRegisterNDeliveriesAsRegisterValue());

		frontend.close();
	}
	
	/* ============== */
	/* Method testing */

	private static void pingTest(String in) {
		try{
			String response = frontend.getPing(in);
			System.out.println("@PingTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@PingTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}

	/* private static void writeTest(String registerId, RegisterValue value) {
		try{
			RegisterRequest request = RegisterRequest.newBuilder()
				.setId(registerId)
				.setData(value)
				.build();
			WriteResponse response = frontend.writeReplicated(request);
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
			ReadResponse response = frontend.readReplicated(request);
			System.out.println("@ReadTest:\n" + response.getData().getRegBalance().getBalance());

			RegisterBalance b = RegisterBalance.getDefaultInstance();
			System.out.println("@ReadTest:\n" + response.getData().getRegBalance().equals(b));
		} catch (StatusRuntimeException e) {
			System.out.println("@ReadTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	} */

}