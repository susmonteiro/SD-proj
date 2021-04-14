package pt.tecnico.rec;

import java.io.IOException;

import java.util.Scanner;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RecordMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	private static String ZooKeeper_IP, IP;
	private static int ZooKeeper_PORT, PORT, instance_num;

	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}



	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(RecordMain.class.getSimpleName());
		
		parseArgs(args);

		final BindableService impl = new RecordServerImpl();

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
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s ZooKeeper_IP ZooKeeper_PORT " + 
				"IP PORT instance_num %n", RecordMain.class.getName());
			System.exit(1);
		}
		
		ZooKeeper_IP = args[0];
		ZooKeeper_PORT = Integer.parseInt(args[1]);
		IP = args[2];
		PORT = Integer.parseInt(args[3]);
		instance_num = Integer.parseInt(args[4]);

	}

	public static String identity() {
		return "Im Rec " + instance_num + " at " + path(); 
	}

	public static String path() {
		return IP + ":" + PORT;
	}
	
}
