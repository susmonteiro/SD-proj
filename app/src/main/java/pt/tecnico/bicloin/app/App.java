package pt.tecnico.bicloin.app;

import java.util.InputMismatchException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.NoSuchElementException;
import java.lang.NumberFormatException;

import io.grpc.StatusRuntimeException;

import pt.tecnico.bicloin.hub.frontend.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;


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

        try {
            checkId(userID);
            checkPhoneNumber(userPhoneNumber);
            checkLatitude(latitude);
            checkLongitude(longitude);
        } catch (InvalidArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(1);	
        }

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

    /**========== Checking User Information ========== */
    public static void checkId(String id) throws InvalidArgumentException {
        if (id.length() < 3 || id.length() > 10) throw new InvalidArgumentException("ID " + id + " is invalid.\nID has to be between 3 and 10 characters.");
    }
    public static void checkPhoneNumber(String phoneNumber) throws InvalidArgumentException {
        if (!phoneNumber.startsWith("+")) throw new InvalidArgumentException("Phone number " + phoneNumber + " is invalid.\nPhone number has to contain a country code}.");
    }
    public static void checkLatitude(float latitude) throws InvalidArgumentException {
        if (latitude < -90 || latitude > 90) throw new InvalidArgumentException("Latitude " + latitude + " is invalid\n.Latitude has to be between -90 and 90.");
    }
    public static void checkLongitude(float longitude) throws InvalidArgumentException {
        if (longitude < -180 || longitude > 180) throw new InvalidArgumentException("Longitude " + longitude + " is invalid\n.Longitude has to be between -180 and 180.");
    }



    public static void start(){

        System.out.println(App.class.getSimpleName());
        
		try (Scanner scanner = new Scanner(System.in)) {
            String input;
            System.out.println("Insira um comando ou escreva exit para sair");
            System.out.print(">");
            do {
                
                
                input = scanner.nextLine();
                String[] inputs = input.split(" ");
                
                    switch(inputs[0]) {
                        case "balance":
                        balance();
                        break;
                    case "top-up":
                        topup(inputs);
                        break;
                    case "tag":
                        tag(inputs);
                        break;
                    case "move":
                        move(inputs);
                        break;
                    case "at":
                        at();
                        break;
                    case "scan":
                        scan(inputs);
                        break;
                    case "info":
                        info(inputs);
                        break;
                    case "bike-up":
                        bikeup(inputs);
                        break;
                    case "bike-down":
                        bikedown(inputs);
                        break;
                    case "ping":
                        ping();
                        break;
                    case "sys-status":
                        sysStatus();
                        break;
                    case "help":
                        help();
                        break;
                    case "exit":
                        hub.close();
                        System.exit(0);
                        break;
                    case "zzz":
                        System.out.print("\b");
                        TimeUnit.MILLISECONDS.sleep(Integer.parseInt(inputs[1]));
                        break;
                    default:
                        System.out.print("\b");
                        break;
                }
                System.out.print(">");
    
            //App closes when user enters command 'exit' or it reaches the EOF
            } while (true);		
        } catch (NoSuchElementException e) {
            System.out.println("Chegou ao fim. Terminando a app...");
            hub.close();
            System.exit(0);
        } catch (InterruptedException e) {
            System.out.println("ERRO - " + e.getMessage());
        }
        hub.close();
        
    }



    /** ==========  COMMANDS ========== */

    private static void balance() {
        try {
            AmountResponse response = hub.doBalanceOperation(_user);
            debug(response);
            System.out.println(_user + " " + response.getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }

    private static void topup(String[] inputs) {

        try {
            int amount = Integer.parseInt(inputs[1]);
        
            AmountResponse response = hub.doTopUpOperation(_user, amount, _phone);
            debug(response);
            System.out.println(_user + " " + response.getBalance() + " BIC");

        } catch (NumberFormatException e) {
            System.out.println("ERRO - Argumentos incorretos para comando top-up!");
        
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando top-up!");
        }
            
    }


    private static void tag(String[] inputs) {
    
        try {
            newlatitude = Float.parseFloat(inputs[1]);
            newlongitude = Float.parseFloat(inputs[2]);
            newloc = inputs[3];
                
            if (!(_tags.containsKey(newloc))) createTag(newlatitude, newlongitude, newloc);
            
            System.out.println("OK");

        } catch (NumberFormatException e) {
            System.out.println("ERRO - Argumentos incorretos para comando tag!");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando tag!");
        }
    }

    

    private static void move(String[] inputs) {
        
        if (inputs.length == 3) {
            // if we want to move to specific coordinates
            moveCoords(inputs);
    
            // if we want to move to previously created tag
        } else if (inputs.length == 2) {
            moveTag(inputs);

    
        } else {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
        }
    }

    
    private static void at() {
        System.out.println(_user + " em https://www.google.com/maps/place/" + String.format("%.04f", _latitude) 
            + "," + String.format("%.04f", _longitude));
    }

    
    private static void scan(String[] inputs) {
        
        try {
            int count = Integer.parseInt(inputs[1]);

            List<String> stations = new ArrayList<String>();
            LocateStationResponse response = hub.doLocateStationOperation(
                _latitude, _longitude, count);
            debug(response);

            stations = response.getStationIdList();

            for (String station : stations) {
                InfoStationResponse infoRes = hub.doInfoStationOperation(station);
                float lat = infoRes.getCoordinates().getLatitude();
                float lon = infoRes.getCoordinates().getLongitude();
                System.out.println(station 
                + ", lat " + String.format("%.04f", lat)
                + ", " + String.format("%.04f", lon)
                + " long, " + infoRes.getNDocks() + " docas, "
                + infoRes.getReward() + " BIC prémio, " 
                + infoRes.getNBicycles() + " bicicletas, " 
                + "a " + (int)Math.round(distance(_latitude, _longitude, lat, lon)) + " metros");
            }
                
        } catch (NumberFormatException e) {
            System.out.println("ERRO - Argumentos incorretos para comando scan!");	
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando scan!");
        }
    }
     
    
    
    private static void info(String[] inputs) {
        try {
            String station = inputs[1];
            if (station == null) {
                System.out.println("ERRO - Argumentos incorretos para comando info!");
                return;
            }
            InfoStationResponse response = hub.doInfoStationOperation(station);
            debug(response);
            System.out.println(response.getName() 
                + ", lat " + String.format("%.04f", response.getCoordinates().getLatitude())
                + ", " + String.format("%.04f", response.getCoordinates().getLongitude())
                + " long, " + response.getNDocks() + " docas, "
                + response.getReward() + " BIC prémio, " 
                + response.getNBicycles() + " bicicletas, " 
                + response.getNPickUps() + " levantamentos, "
                + response.getNDeliveries() + " devoluções.");
                    
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando info!");
        }
        
    }
    

    private static void bikeup(String[] inputs) {
        
        try {
            String station = inputs[1];
            if (station == null) {
                System.out.println("ERRO - Argumentos incorretos para comando bike-up!");
                return;
            }
            hub.doBikeUpOperation(_user, _latitude, _longitude, station);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());    
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando bike-up!");
        }
    
    }

    private static void bikedown(String[] inputs) {
        try {
            String station = inputs[1];
            if (station == null) {
                System.out.println("ERRO - Argumentos incorretos para comando bike-down!");
                return;
            }
            hub.doBikeDownOperation(_user, _latitude, _longitude, station);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("ERRO - Argumentos incorretos para comando bike-down!");
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
    
    private static void sysStatus() {
        try {
            SysStatusResponse response = hub.doSysStatusOperation();
            int len = response.getServerStatusList().size();
            for (int i=0; i<len; i++) {
                boolean status = (response.getServerStatus(i).getStatus()) ? true : false; 
                System.out.println("Path: " + response.getServerStatus(i).getPath() +
                    " Status: " + status);
            }
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }
    

    private static void help() {
        System.out.print("Pode introduzir os seguintes comandos:\n" +
        "\n" +
        "balance\n" + 
        "top-up [quantia]\n" +
        "tag [latitude] [longitude] [nomeTag]\n" + 
        "move [nomeTag]\n" +
        "move [latitude] [longitude]\n" +
        "at\n" +
        "scan [numero de estacoes]\n" +
        "info [nome da estacao]\n" +
        "bike-up [nome da estacao]\n" +
        "bike-down [nome da estacao]\n");
    }




    /**============ AUXILIAR METHODS ============ */


    private static void createTag(float newlatitude, float newlongitude, String newloc) {
        Tag tag = new Tag(newlatitude, newlongitude, newloc);
        _tags.put(newloc, tag);
    }

    private static void moveTag(String[] inputs) {
        
        newloc = inputs[1];

        if (newloc == null) {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
            return;
        }
    
        if ((newloc != null) && (_tags.containsKey(newloc))) {

            _latitude = _tags.get(newloc).lat;
            _longitude = _tags.get(newloc).lon;
    
            at();
        } else {
            System.out.println("ERRO - Localizacao nao esta guardada!");
        }
          
    }

    private static  void moveCoords(String[] inputs) {
        try {
            _latitude = Float.parseFloat(inputs[1]);
            _longitude = Float.parseFloat(inputs[2]);
            at();
            
        } catch (NumberFormatException e) {
            System.out.println("ERRO - Argumentos incorretos para comando move!");
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

}
