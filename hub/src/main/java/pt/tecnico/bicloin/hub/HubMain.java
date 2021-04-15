package pt.tecnico.bicloin.hub;

import java.io.IOException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidFileInputException;

import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.bicloin.hub.domain.*;

public class HubMain {
	private static final boolean DEBUG = (System.getProperty("debug") != null);
	private static final int USER_FILE_FIELDS = 3;
	private static final int STATION_FILE_FIELDS = 7;
	
	
	private static String recIP, IP;
	private static int recPORT, PORT, instance_num;
	private static String usersFile, stationsFile;
	private static boolean initRec = false;


	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(HubMain.class.getSimpleName());
		
		parseArgs(args);

		// Initialize service (and Load data)
		final HubServerImpl impl = new HubServerImpl(recIP, recPORT, parseUsers(usersFile), parseStations(stationsFile), DEBUG);

		// Initilize register in Record
		if (initRec) impl.getHub().initializeRec();

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

	private static void parseArgs(String[] args) {
		// Receive and print arguments.
		debug(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
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

	private static Map<String, User> parseUsers(String path) {
		debug("@HubMain Parsing Users...");
		Map<String, User> users = new HashMap<String, User>();

		try (Scanner scanner = new Scanner(new File(usersFile))) {

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
		
		try (Scanner scanner = new Scanner(new File(stationsFile))) {

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
		return "Im Hub " + instance_num + " at " + path(); 
	}
	
	public static String path() {
		return IP + ":" + PORT;
	}	

	/** Helper method to print debug messages. */
	private static void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@HubMain\t" + debugMessage);
	}
}
