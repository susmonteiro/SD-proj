package pt.tecnico.rec;

import java.io.IOException;

import java.util.Scanner;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import static pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.ZOO_DIR;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	private static final boolean DEBUG_TEST = (System.getProperty("debugDemo") != null);
	private static String zooHost, IP, server_path;
	private static int zooPort, PORT, instance_num;
	/** ZooKeeper helper object. */
	private static Server server = null;
	private static ZKNaming zkNaming = null;


	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(RecordMain.class.getSimpleName());

		// Use hook to register a thread to be called on shutdown.
		Runtime.getRuntime().addShutdownHook(new Unbind());

		// Create new thread where we wait for the user input.
		new Thread(() -> {
			System.out.println("<Press enter to shutdown>");
			new Scanner(System.in).nextLine();
			
			System.exit(0);
		}).start();

		parseArgs(args);
		debugDemo("Replica " + instance_num + " starting...");

		final BindableService impl = new RecordServerImpl();

		// Create a new server to listen on port.
		server = ServerBuilder.forPort(PORT).addService(impl).build();
		
		// Register on ZooKeeper.
		debugDemo("Contacting ZooKeeper at " + zooHost + ":" + zooPort);
		zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));

		debugDemo("Binding " + server_path + " to " + IP + ":" + PORT);
		zkNaming.rebind(server_path, IP, Integer.toString(PORT));
		
		// Start the server.
		server.start();
		
		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

	public static void parseArgs(String[] args) {
		// receive and print arguments
		debug("Received " + args.length + " arguments");
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
		}

		// Check arguments.
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort " + 
				"IP PORT instance_number %n", RecordMain.class.getName());
			System.exit(1);
		}
		
		zooHost = args[0];
		zooPort = Integer.parseInt(args[1]);
		IP = args[2];
		PORT = Integer.parseInt(args[3]);
		instance_num = Integer.parseInt(args[4]);
		server_path = ZOO_DIR + '/' + instance_num;		// path to server logged to zookeeper
		debug("Path: "+ server_path);
	}

	public static String identity() {
		return "Im Rec " + instance_num + " at " + server_path; 
	}

	/** 
	 * Unbind class unbinds replica from ZKNaming after interruption.
	 */
	static class Unbind extends Thread {
		public void run() {
			if(server != null)	{ server.shutdown(); }
			if (zkNaming != null) {
				try {
					System.out.println("Unbinding " + server_path + " from ZooKeeper...");
					zkNaming.unbind(server_path, IP, String.valueOf(PORT));
				}
			   	catch (ZKNamingException e) {
					System.err.println("Could not close connection with ZooKeeper: " + e);
					return;
				}
			}
		}
	}

	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static void debugDemo(Object debugMessage) {
		if (DEBUG_TEST || DEBUG_FLAG)
			System.err.println(debugMessage);
	}	
}
