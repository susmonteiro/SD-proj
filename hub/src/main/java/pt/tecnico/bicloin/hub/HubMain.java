package pt.tecnico.bicloin.hub;

import java.io.IOException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidFileInputException;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.ZOO_DIR;

import pt.tecnico.bicloin.hub.domain.*;

public class HubMain {
	private static final boolean DEBUG = (System.getProperty("debug") != null);
	private static final boolean DEBUG_TEST = (System.getProperty("debugDemo") != null);
	private static final int USER_FILE_FIELDS = 3;
	private static final int STATION_FILE_FIELDS = 7;
	
	private static String zooHost, IP, server_path;
	private static int zooPort, PORT, instance_num;
	private static String usersFile, stationsFile;
	private static boolean initRec = false;

	private static HubServerImpl impl = null;
	private static Server server = null;
	private static ZKNaming zkNaming = null;

	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(HubMain.class.getSimpleName());

		// Use hook to register a thread to be called on shutdown.
		Runtime.getRuntime().addShutdownHook(new CleanUp());

		// Create new thread where we wait for the user input.
		new Thread(() -> {
			System.out.println("<Press enter to shutdown>");
			new Scanner(System.in).nextLine();
			
			System.exit(0);
		}).start();
		
		parseArgs(args);
		debugDemo("Hub " + instance_num + " starting...");

		// Initialize service (and Load data)
		impl = new HubServerImpl(zooHost, zooPort, instance_num, parseUsers(usersFile), parseStations(stationsFile), DEBUG);

		// Initilize register in Record
		if (initRec) impl.getHub().initializeRec();

		// Create a new server to listen on port.
		server = ServerBuilder.forPort(PORT).addService((BindableService)impl).build();

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

	private static void parseArgs(String[] args) {
		// Receive and print arguments.
		debug(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
		}

		// Check arguments.
		if (args.length < 7) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort " + 
				"IP PORT instance_num users.csv stations.csv [initRec] %n", HubMain.class.getName());
			System.exit(1);
		}
		
		zooHost = args[0];
		zooPort = Integer.parseInt(args[1]);
		IP = args[2];
		PORT = Integer.parseInt(args[3]);
		instance_num = Integer.parseInt(args[4]);
		server_path = ZOO_DIR + '/' + instance_num;
		debug("Path: "+ server_path);
		usersFile = args[5];
		stationsFile = args[6];
		initRec = args[args.length-1].equals("initRec");
	}

	private static Map<String, User> parseUsers(String path) {
		debug("@HubMain Parsing Users...");
		Map<String, User> users = new HashMap<String, User>();

		try (Scanner scanner = new Scanner(new File(usersFile), "utf-8")) {

			String CSV_DELIMITER = ",";

			// read fields and populate Users
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				debug(line);
				String[] fields = line.split(CSV_DELIMITER);
				if (fields.length < USER_FILE_FIELDS)
				throw new InvalidFileInputException("User file: wrong format. Lines should have " + USER_FILE_FIELDS + " fields.");
			
				users.put(fields[User.ID_IDX], new User(fields));
			}

		} catch(InvalidFileInputException e) {
			System.out.println(e.getMessage());
			System.exit(1);		// terminate server
		} catch (IOException e) {
			e.printStackTrace();
		}
		return users;
	}

	private static Map<String, Station> parseStations(String path) {
		debug("@HubMain Parsing Stations...");
		Map<String, Station> stations = new HashMap<String, Station>();
		
		try (Scanner scanner = new Scanner(new File(stationsFile), "utf-8")) {

			String CSV_DELIMITER = ",";

			// read fields and populate Users
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				debug(line);
				String[] fields = line.split(CSV_DELIMITER);
				if (fields.length < STATION_FILE_FIELDS)
					throw new InvalidFileInputException("Station file: wrong format. Lines should have " + STATION_FILE_FIELDS + " fields.");
				stations.put(fields[Station.ID_IDX], new Station(fields));
			}

		} catch(InvalidFileInputException e) {
			System.out.println(e.getMessage());
			System.exit(1);		// terminate server
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stations;
	}
	
	public static String identity() {
		return "Im Hub " + instance_num + " at " + getPath(); 
	}
	
	public static String getPath() {
		return server_path;
	}	


	/** 
	 * Clean up method 
	 */
	static class CleanUp extends Thread {
		public void run() {
			if(server != null)	{ server.shutdown(); }
			if(impl != null)	{ impl.shutdown();	}		// close runtime connections (Record frontend)			
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
		if (DEBUG)
			System.err.println("@HubMain\t" + debugMessage);
	}

	public static void debugDemo(Object debugMessage) {
		if (DEBUG_TEST || DEBUG)
			System.err.println(debugMessage);
	}
}
