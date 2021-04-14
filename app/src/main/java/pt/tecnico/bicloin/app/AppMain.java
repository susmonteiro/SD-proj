package pt.tecnico.bicloin.app;

import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class AppMain {
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	

	private static String hubIP;
	private static int hubPORT;
	private static String userID, userPhoneNumber;
	private static float latitude, longitude;


	/** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	public static void main(String[] args) {
		System.out.println(AppMain.class.getSimpleName());
		
		parseArgs(args);
		
		App app = new App();
		app.start(hubIP, hubPORT, userID, userPhoneNumber, latitude, longitude);
		/* //TODO apanhar excecao Mismatch input
		while(true){
			System.out.print(">");
			try (Scanner in = new Scanner(System.in)) {
			String input = in.nextLine();
			debug(input);
			} catch(InputMismatchException e) {
				System.out.println(e.getMessage());
				System.exit(1);		
			}
		}
		//in.close();
		//System.out.println(s); */
		
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
			System.err.printf("Usage: java %s hubIP hubPORT " + 
				"userID userPhoneNumber latitude longitude %n", AppMain.class.getName());
			System.exit(1);
		}
		
		hubIP = args[0];
		hubPORT = Integer.parseInt(args[1]);
		userID = args[2];
		userPhoneNumber = args[3];
		latitude = Float.parseFloat(args[4]);
		longitude = Float.parseFloat(args[5]);
	}

}
