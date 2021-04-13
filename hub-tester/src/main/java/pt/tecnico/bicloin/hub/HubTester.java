package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;

import pt.tecnico.bicloin.hub.frontend.HubFrontend;

public class HubTester {
	
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

		HubFrontend frontend = new HubFrontend(host, port);
		
		try{
			PingRequest request = PingRequest.newBuilder().setInput("Boss").build();
			PingResponse response = frontend.ping(request);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		}

		SysStatusRequest request = SysStatusRequest.newBuilder().build();
		SysStatusResponse response = frontend.sysStatus(request);
		System.out.println(response);

		frontend.close();
	}
	
}
