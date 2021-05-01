package pt.tecnico.rec;

import java.io.IOException;

import java.util.Scanner;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	private static String zooHost, IP, server_path;
	private static int zooPort, PORT;
	/** ZooKeeper helper object. */
	private static ZKNaming zkNaming = null;


	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(RecordMain.class.getSimpleName());
		
		parseArgs(args);

		final BindableService impl = new RecordServerImpl();

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(PORT).addService(impl).build();
		
		// Register on ZooKeeper.
		try {
			System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");
			zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));

			System.out.println("Binding " + server_path + " to " + IP + ":" + PORT + "...");
			zkNaming.rebind(server_path, IP, Integer.toString(PORT));
			
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
			
		} finally {
			if (zkNaming != null) {
				zkNaming.unbind(server_path, IP, Integer.toString(PORT));			
			}
		}
		

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
			System.err.printf("Usage: java %s zooHost zooPort " + 
				"IP PORT server_path %n", RecordMain.class.getName());
			System.exit(1);
		}
		
		zooHost = args[0];
		zooPort = Integer.parseInt(args[1]);
		IP = args[2];
		PORT = Integer.parseInt(args[3]);
		server_path = args[4];

	}

	public static String identity() {
		return "Im Rec " + server_path + " at " + path(); 
	}

	public static String path() {
		return IP + ":" + PORT;
	}

	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}
	
}
