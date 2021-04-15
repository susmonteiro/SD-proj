package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.*;
import io.grpc.StatusRuntimeException;

import pt.tecnico.bicloin.hub.frontend.HubFrontend;
import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


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

		System.out.println();

		final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		frontend = new HubFrontend(host, port);

		System.out.println("=== Tests ===\n");

		/* Balance */
		balanceTest("friend");

		/* TopUp */
		topUpTest("friend", 10, "+351123456789");

		/* InfoStation */
		infoStationTest("ista");

		/* LocateStation */
		locateStationTest(11.1111f, 22.2222f, 3);

		/* BikeUp */
		bikeUpTest("friend", 11.1111f, 22.2222f, "ista");

		/* BikeDown */
		bikeDownTest("friend", 11.1111f, 22.2222f, "ista");

		/* Ping */
		pingTest("boss");
		pingTest("");

		/* SysStatus */
		sysStatusTest();
		

		frontend.close();
	}

	/* ===    Remote functions tests   === */

	private static void balanceTest(String userId) {
		System.out.println("@BalanceTest('" + userId + "')");

		try {
			AmountResponse response = frontend.doBalanceOperation(userId);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void topUpTest(String userId, int amount, String phoneNumber) {
		System.out.println("@TopUpTest('" + userId + "', " + amount + ", '" + phoneNumber + "')");

		try {
			AmountResponse response = frontend.doTopUpOperation(userId, amount, phoneNumber);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void infoStationTest(String stationId) {
		System.out.println("@InfoStationTest('" + stationId + "')");

		try {
			InfoStationResponse response = frontend.doInfoStationOperation(stationId);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void locateStationTest(float latitude, float longitude, int nStations) {
		System.out.println("@LocateStationTest(" + latitude + ", " + longitude + ", " + nStations + ")");

		try {
			LocateStationResponse response = frontend.doLocateStationOperation(latitude, longitude, nStations);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void bikeUpTest(String userId, float latitude, float longitude, String stationId) {
		System.out.println("@BikeUpTest('" + userId + "', " + latitude + ", " + longitude + ", '" + stationId + "')");

		try {
			frontend.doBikeUpOperation(userId, latitude, longitude, stationId);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void bikeDownTest(String userId, float latitude, float longitude, String stationId) {
		System.out.println("@BikeDownTest'" + userId + "', " + latitude + ", " + longitude + ", '" + stationId + "')");

		try {
			frontend.doBikeDownOperation(userId, latitude, longitude, stationId);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void pingTest(String input) {
		System.out.println("@PingTest('" + input + "')");

		try {
			PingResponse response = frontend.doPingOperation(input);
			System.out.println(response);
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}

	private static void sysStatusTest() {
		System.out.println("@SysStatusTest()");

		try {
			frontend.doSysStatusOperation();
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription() + "\n");
		}
	}
	
}
