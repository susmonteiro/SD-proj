package pt.tecnico.bicloin.hub;

import java.io.IOException;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class HubMain {
	private static String recIP, IP;
	private static int recPORT, PORT, instance_num;
	private static String usersFile, stationsFile;
	private static boolean initRec = false;

	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(HubMain.class.getSimpleName());
		
		parseArgs(args);

		final BindableService impl = new HubServerImpl(recIP, recPORT);

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(PORT).addService(impl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Server started");

		// Create new thread where we wait for the user input.
		new Thread(() -> {
			System.out.println("<Press enter to shutdown>");
			new Scanner(System.in).nextLine();

			server.shutdown();
		}).start();

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

	public static void parseArgs(String[] args) {
		// Receive and print arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 7) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s recIP recPORT " + 
				"IP PORT instance_num users.csv stations.csv [initRec] %n", HubMain.class.getName());
			System.exit(1);
		}
		
		recIP = args[0];
		recPORT = Integer.parseInt(args[1]);
		IP = args[2];
		PORT = Integer.parseInt(args[3]);
		instance_num = Integer.parseInt(args[4]);
		usersFile = args[5];
		stationsFile = args[6];
		initRec = args[args.length-1].equals("initRec");
	}

	public static String identity() {
		return "Im Hub " + instance_num + " at " + path(); 
	}
	
	public static String path() {
		return IP + ":" + PORT;
	}
}
