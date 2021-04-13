package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;

import pt.tecnico.bicloin.hub.frontend.HubFrontend;


public class HubTester {
	private static HubFrontend frontend;
	
	public static void main(String[] args) {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", HubTester.class.getName());
			return;
		}

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		frontend = new HubFrontend(host, port);

		
		/* ===    Remote functions    === */

		/* Ping */
		pingTest("boss");
		pingTest("");

		/* SysStatus */
		sysStatusTest();
		

		frontend.close();
	}

	private static void pingTest(String input) {
		System.out.println("@Ping...");

		try{
			PingRequest request = PingRequest.newBuilder().setInput(input).build();
			PingResponse response = frontend.ping(request);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		}
	}

	private static void sysStatusTest() {
		System.out.println("@SysStatus...");

		SysStatusRequest request = SysStatusRequest.newBuilder().build();
		SysStatusResponse response = frontend.sysStatus(request);
		System.out.println(response);
	}
	
}
