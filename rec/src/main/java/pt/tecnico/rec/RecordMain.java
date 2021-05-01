package pt.tecnico.rec;

import java.io.IOException;

import java.util.Scanner;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import static pt.tecnico.rec.frontend.RecordFrontend.ZOO_DIR;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	private static String zooHost, IP, server_path;
	private static int zooPort, PORT, instance_num;
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
			debug("Contacting ZooKeeper at " + zooHost + ":" + zooPort);
			zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));

			debug("Binding " + server_path + " to " + IP + ":" + PORT);
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
		debug("Received" + args.length + "arguments");
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
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
		instance_num = Integer.parseInt(args[4]);
		server_path = ZOO_DIR + instance_num;		// path to server logged to zookeeper
		debug("Path: "+ server_path);
	}

	public static String identity() {
		return "Im Rec " + instance_num + " at " + server_path; 
	}

	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}
	
}
