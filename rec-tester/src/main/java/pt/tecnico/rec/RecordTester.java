package pt.tecnico.rec;

import java.util.Map;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;
import static pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.*;
import static pt.tecnico.rec.frontend.MessageHelper.*;
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
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", RecordTester.class.getName());
			return;
		}

		final String zooHost = args[0];
		final int zooPort = Integer.parseInt(args[1]);

		frontend = new RecordFrontendReplicationWrapper(zooHost, zooPort, 1);
		

		/* Ping */
		int recInstanceToTest = 1;
		frontend.getPing("friend", recInstanceToTest);
		// frontend.getPing("", recInstanceToTest);

		/* Write */
		frontend.setBalance(registerIdDefault, 10);
		// frontend.setOnBike(registerIdDefault, true);
		// frontend.setNBikes(registerIdDefault, 2);
		// frontend.setNPickUps(registerIdDefault, 3);
		// frontend.setNDeliveries(registerIdDefault, 4);

		/* Read */
		System.out.println("@ReadTest:\n" + frontend.getBalance(registerIdDefault));
		// System.out.println("@ReadTest:\n" + frontend.getOnBike(registerIdDefault));
		// System.out.println("@ReadTest:\n" + frontend.getNBikes(registerIdDefault));
		// System.out.println("@ReadTest:\n" + frontend.getNPickUps(registerIdDefault));
		// System.out.println("@ReadTest:\n" + frontend.getNDeliveries(registerIdDefault));

		/* test for empty requests */
		/* RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterTag emptyTag = RegisterTag.newBuilder().build();
        RegisterRequest request = getRegisterRequest("alice", emptyVal, emptyTag);

		try {
			frontend.writeReplicated(request);
		} catch (StatusRuntimeException e) {
			System.out.println("@WBlaaariteTest:\nCaught exception with description: " +
		e.getStatus().getDescription());
			System.out.println("Oh rip we got an exception");
		} */
		
		frontend.close();
	}
	
	/* ============== */
	/* Method testing */

	/* private static void pingTest(String in) {
		try{
			PingResponse response = frontend.getPing(in, );
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
				.setData(RegisterData.newBuilder()
					.setValue(value)
					.setTag(RegisterTag.newBuilder()
						.setSeqNumber(1)
						.setClientID(1)
						.build()
					)
					.build()
				)
				.build();
			frontend.writeReplicated(request);
			// System.out.println("@WriteTest:\n" + response);
		} catch (StatusRuntimeException e) {
			System.out.println("@WriteTest:\nCaught exception with description: " +
				e.getStatus().getDescription());
		}
	}
 */
	/*private static void readTest(String registerId, RegisterValue value) {
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