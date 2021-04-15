package pt.tecnico.bicloin.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.tecnico.bicloin.hub.frontend.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.*;


public class App {
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static String _host;
    private static int _port;
    private static String _user, _phone;
    private static float _latitude, _longitude;

    private static final int EARTH_RADIUS = 6371;

    private static float newlatitude, newlongitude;
    private static String newloc;
    private static Map<String, Tag> _tags;

    private static HubFrontend hub;

    private static class Tag {
        private float lat;
        private float lon;
        private String tagname;

        public Tag(float latitude, float longitude, String tname) {
            lat = latitude;
            lon = longitude;
            tagname = tname;
        }
    }
    
    public App(String hubIP, int hubPORT, String userID, String userPhoneNumber, 
        float latitude, float longitude) {
        _host = hubIP;
        _port = hubPORT;
        _user = userID;
        _phone = userPhoneNumber;
        _latitude = latitude;
        _longitude = longitude;
        
        _tags = new HashMap<String, Tag>();

        hub = new HubFrontend(hubIP, hubPORT);
    }

    /** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    public static void start(){

        System.out.println(App.class.getSimpleName());
        
		try (Scanner scanner = new Scanner(System.in)) {
            String input;
            System.out.println("Insira um comando ou escreva exit para sair");
            do {
                System.out.print(">");
                    input = scanner.next();
                    //debug(input);

                    switch(input) {
                        case "balance":
                        balance();
                        break;
                    case "top-up":
                        topup(scanner);
                        break;
                    case "tag":
                        tag(scanner);
                        break;
                    case "move":
                        move(scanner);
                        break;
                    case "at":
                        at();
                        break;
                    case "scan":
                        scan(scanner);
                        break;
                    case "info":
                        info(scanner);
                        break;
                    case "bike-up":
                        bikeup(scanner);
                        break;
                    case "bike-down":
                        bikedown(scanner);
                        break;
                    case "ping":
                        ping();
                        break;
                    case "sys-status":
                        break;
                    case "help":
                        break;
                }
    
            //App closes when user enters command 'exit'
            } while (!input.equals("exit"));
            scanner.close();
		} catch(InputMismatchException e) {
			System.out.println(e.getMessage());
			System.exit(1);		
		}
		

        hub.close();
        
    }


    private static void balance() {
        try {
            AmountResponse response = hub.doBalanceOperation(_user);
            System.out.println(_user + " " + response.getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }

    private static void topup(Scanner scanner) {
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) {  
            System.out.println("ERRO - Faltam argumentos: Quantia a carregar!");
            return;
        }
        try {
            if (scanner.hasNextInt()) {
                int amount = scanner.nextInt();
                debug(amount);
        
            AmountResponse response = hub.doTopUpOperation(_user, amount, _phone);
            System.out.println(_user + " " + response.getBalance() + " BIC");
            } else {
                System.out.println("ERRO - Argumentos incorretos para comando top-up!");
                scanner.nextLine();
            }
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
            
    }


    private static void tag(Scanner scanner) {
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) {  
            System.out.println("ERRO - Faltam argumentos: Coordenadas e Nome da Tag!");
            return;
        }
    
        try {
            newlatitude = scanner.nextFloat();
    
            if (scanner.findInLine("") != null) newlongitude = scanner.nextFloat();
            if (scanner.findInLine("") != null) { newloc = scanner.next();
            } else {
                System.out.println("ERRO - Faltam argumentos: Coordenadas e Nome da Tag!");
                return;
            }
                
            if (!(_tags.containsKey(newloc))) createTag(newlatitude, newlongitude, newloc);
            
            System.out.println("OK");
        } catch (InputMismatchException e) {
            System.out.println("ERRO - Argumentos incorretos para comando tag!");
            scanner.nextLine();
        }
    }

    private static void createTag(float newlatitude, float newlongitude, String newloc) {
        Tag tag = new Tag(newlatitude, newlongitude, newloc);
        _tags.put(newloc, tag);
    }

    private static void move(Scanner scanner) {
        
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) { 
            System.out.println("ERRO - Faltam argumentos: Coordenadas ou Tag!");
            return;
        }
    
        // if we want to move to specific coordinates
        if (scanner.hasNextFloat()){
            moveCoords(scanner);
    
        // if we want to move to previously created tag
        } else if (scanner.hasNext()) {
            moveTag(scanner);
    
        } else {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
        }
    }

    private static  void moveCoords(Scanner scanner) {
        try {
            _latitude = scanner.nextFloat();
                
            if (scanner.findInLine("") != null) {
                _longitude = scanner.nextFloat();
                at();
            } else {  
                System.out.println("ERRO - Faltam argumentos: [latitude] [longitude] !"); 
            }
            
        } catch (InputMismatchException e) {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
            scanner.nextLine();
        }
    }
    
    private static void moveTag(Scanner scanner) {
        try {
            newloc = scanner.next();
    
                if ((newloc != null) && (_tags.containsKey(newloc))) {

                    _latitude = _tags.get(newloc).lat;
                    _longitude = _tags.get(newloc).lon;
    
                    at();
                    scanner.nextLine();
                } else {
                    System.out.println("ERRO - Localizacao nao esta guardada!");
                    scanner.nextLine();
                }
        } catch (InputMismatchException e) {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
            scanner.nextLine();
        }
    }

    private static void at() {
        System.out.println(_user + " em " + _latitude + "," + _longitude);
    }

    
    private static void scan(Scanner scanner) {
    
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) {  
            System.out.println("ERRO - Faltam argumentos: Numero de estacoes!");
            return;
        }
        if (scanner.hasNextInt()) {
            try {
                List<String> stations = new ArrayList<String>();
                int count = scanner.nextInt();
                LocateStationResponse response = hub.doLocateStationOperation(
                    _latitude, _longitude, count);
                stations = response.getStationIdList();
                for (String station : stations) {
                    InfoStationResponse infoRes = hub.doInfoStationOperation(station);
                    float lat = infoRes.getCoordinates().getLatitude();
                    float lon = infoRes.getCoordinates().getLongitude();
                    System.out.println(station 
                    + ", lat " + lat
                    + ", " + lon
                    + " long, " + infoRes.getNDocks() + " docas, "
                    + infoRes.getReward() + " BIC prémio, " 
                    + infoRes.getNBicycles() + " bicicletas, " 
                    + "a " + (int)Math.round(distance(_latitude, _longitude, lat, lon)) + " metros");
                }
                
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando scan!");	
            } catch (StatusRuntimeException e) {
                System.out.println("ERRO - " + e.getMessage());
            } 
        }
     
    }

    public static double distance(float s1Lat, float s1Long, float s2Lat, float s2Long) {		

        double aLat = (double) s1Lat;
		double bLat = (double) s2Lat;
		double aLong = (double) s1Long;
		double bLong = (double) s2Long;
		
		double latitude  = Math.toRadians((bLat - aLat));
        double longitude = Math.toRadians((bLong - aLong));

		aLat = Math.toRadians(aLat);
        bLat = Math.toRadians(bLat);

        double a = haversin(latitude) + Math.cos(aLat) * Math.cos(bLat) * haversin(longitude);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;
		return distance * 1000; 
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
    
    

    private static void info(Scanner scanner) {
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) { 
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNext()) {
            try {
                String station = scanner.next();
                InfoStationResponse response = hub.doInfoStationOperation(station);
                System.out.println(response.getName() 
                    + ", lat " + response.getCoordinates().getLatitude()
                    + ", " + response.getCoordinates().getLongitude()
                    + " long, " + response.getNDocks() + " docas, "
                    + response.getReward() + " BIC prémio, " 
                    + response.getNBicycles() + " bicicletas, " 
                    + response.getNPickUps() + " levantamentos, "
                    + response.getNDeliveries() + " devoluções.");
                    
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando info!");
            } catch (StatusRuntimeException e) {
                System.out.println("ERRO - " + e.getMessage());
            } 
        }
    }
    

    private static void bikeup(Scanner scanner) {
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) {  
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNext()) {
            try {
                String station = scanner.next();
                hub.doBikeUpOperation(_user, _latitude, _longitude, station);
                System.out.println("OK");
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando bike-up!");
            } catch (StatusRuntimeException e) {
                System.out.println("ERRO - " + e.getMessage());
            } 
        }
    }

    private static void bikedown(Scanner scanner) {
        if ((scanner.findInLine("") == null) || (scanner.findInLine("\t") == null)) { 
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNext()) {
            try {
                String station = scanner.next();
                hub.doBikeDownOperation(_user, _latitude, _longitude, station);
                System.out.println("OK");
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando bike-down!");
            } catch (StatusRuntimeException e) {
                System.out.println("ERRO - " + e.getMessage());
            } 
        }
    }
    



    private static void ping() {
        try {
            PingResponse response = hub.doPingOperation(_user);
            System.out.println(response.getOutput());
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }
    /*
    private static void sysStatus() {
        try {
            SysStatusResponse response = hub.doPingOperation(_user);
            System.out.println(response.getOutput());
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }
    */

}
