package pt.tecnico.bicloin.app;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class AppMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	

	private static String zooHost;
	private static int zooPort;
	private static String userID, userPhoneNumber;
	private static float latitude, longitude;


	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static void main(String[] args) throws ZKNamingException {
		System.out.println(AppMain.class.getSimpleName());
		
		parseArgs(args);
		
		App app = new App(zooHost, zooPort, userID, userPhoneNumber, latitude, longitude);
		app.start();
	}

	private static void parseArgs(String[] args) {
		// Receive and print arguments.
		debug(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
		}

		// Check arguments.
		if (args.length < 6) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort " + 
				"userID userPhoneNumber latitude longitude %n", AppMain.class.getName());
			System.exit(1);
		}
		
		zooHost = args[0];
		zooPort = Integer.parseInt(args[1]);
		userID = args[2];
		userPhoneNumber = args[3];
		latitude = Float.parseFloat(args[4]);
		longitude = Float.parseFloat(args[5]);
	}

}
