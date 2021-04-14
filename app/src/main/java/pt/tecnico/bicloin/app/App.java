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

import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.*;


public class App {
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static String _host;
    private static int _port;
    private static String _user, _phone;
    private static float _latitude, _longitude;

    private static float newlatitude, newlongitude;
    private static String newloc;
    private static Map<String, Tag> _tags;

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
    }

    /** Helper method to print debug messages. */
	public static void debug(Object debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    public static void start(){

        final String target = _host + ":" + _port;

        System.out.println(App.class.getSimpleName());
        
        // Channel is the abstraction to connect to a service endpoint.
	    // Let us use plaintext communication because we do not have certificates.
	    final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        
		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub, but an async stub,
		// or an async stub with Future are always possible.
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
        
        
        
		try (Scanner scanner = new Scanner(System.in)) {
            String input;
            System.out.println("Insira um comando ou escreva exit para sair");
            do {
                System.out.print(">");
                    input = scanner.next();
                    //debug(input);

                    switch(input) {
                        case "balance":
                        debug("chose balance");
                        balance(stub);
                        break;
                    case "top-up":
                        topup(stub, scanner);
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
                        scan(stub, scanner);
                        break;
                    case "info":
                        info(stub, scanner);
                        break;
                    case "bike-up":
                        bikeup(stub, scanner);
                        break;
                    case "bike-down":
                        bikedown(stub, scanner);
                        break;
                    case "ping":
                        ping(stub);
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
		

        // A Channel should be shutdown before stopping the process.
		channel.shutdownNow();
        
    }


    private static void balance(HubServiceGrpc.HubServiceBlockingStub stub) {
        try {
            BalanceRequest request = BalanceRequest.newBuilder().setUserId(_user).build();
            AmountResponse response = stub.balance(request);
            System.out.println(_user + response.getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
    }

    private static void topup(HubServiceGrpc.HubServiceBlockingStub stub, Scanner scanner) {
        if (scanner.findInLine("") == null) { 
            System.out.println("ERRO - Faltam argumentos: Quantia a carregar!");
            return;
        }
        try {
            if (scanner.hasNextInt()) {
                int amount = scanner.nextInt();
                debug(amount);
            
         
            TopUpRequest request = TopUpRequest.newBuilder()
                .setUserId(_user)
                .setAmount(amount)
                .setPhoneNumber(_phone)
                .build();
            AmountResponse response = stub.topUp(request);
            System.out.println(_user + response.getBalance() + " BIC");
            } else {
                System.out.println("ERRO - Argumentos incorretos para comando top-up!");
                scanner.nextLine();
            }
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO - " + e.getMessage());
        } 
            
    }


    private static void tag(Scanner scanner) {
        if (scanner.findInLine("") == null) { 
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
        
        if (scanner.findInLine("") == null) { 
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

    
    private static void scan(HubServiceGrpc.HubServiceBlockingStub stub, Scanner scanner) {
    /*
        if (scanner.findInLine("") == null) { 
            System.out.println("ERRO - Faltam argumentos: Numero de estacoes!");
            return;
        }
        if (scanner.hasNextInt()) {
            try {
                List<String> stations = new ArrayList<String>();
                int count = scanner.nextInt();
                for (int i=0; i<count; i++) {
                    LocateStationRequest request = LocateStationRequest.newBuilder()
                        .setCoordinates(Coordinates.newBuilder()
                        .setLatitude(_latitude)
                        .setLongitude(_longitude)
                        .build()
                    ).setNStations(count)
                    .build();
                    LocateStationResponse response = stub.locateStation(request);
                    stations = response.getStationIdList();
                }
                
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando scan!");
                //TO CHECK mensagem de erro que queremos responder	
            }
        }
     */
    }
    
    

    private static void info(HubServiceGrpc.HubServiceBlockingStub stub, Scanner scanner) {
        if (scanner.findInLine("") == null) { 
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNextInt()) {
            try {
                String station = scanner.next();
                InfoStationRequest request = InfoStationRequest.newBuilder()
                    .setStationId(station)
				    .build();
                InfoStationResponse response = stub.infoStation(request);
                System.out.println(station 
                    + ", lat " + response.getCoordinates().getLatitude()
                    + ", " + response.getCoordinates().getLongitude()
                    + " long," + response.getNDocks() + "docas, "
                    + response.getReward() + " BIC prémio, " 
                    + response.getNBicycles() + " bicicletas, " 
                    + response.getNPickUps() + " levantamentos, "
                    + response.getNDeliveries() + " devoluções.");
                    
            } catch (InputMismatchException e) {
                //System.out.println("ERRO - Argumentos incorretos para comando info!");
                System.out.println("ERRO fora de alcance");
                //TO CHECK mensagem de erro que queremos responder e excecao que recebemos
            } 
        }
    }
    

    private static void bikeup(HubServiceGrpc.HubServiceBlockingStub stub, Scanner scanner) {
        if (scanner.findInLine("") == null) { 
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNext()) {
            try {
                String station = scanner.next();
                BikeRequest request = BikeRequest.newBuilder()
                    .setUserId(_user)
                    .setCoordinates(Coordinates.newBuilder()
					    .setLatitude(_latitude)
					    .setLongitude(_longitude)
					    .build()
				    ).setStationId(station)
				    .build();
                BikeResponse response = stub.bikeUp(request);
                System.out.println("OK");
            } catch (InputMismatchException e) {
                System.out.println("ERRO - Argumentos incorretos para comando bike-up!");
                //TO CHECK mensagem de erro que queremos responder	
            }
        }
    }

    private static void bikedown(HubServiceGrpc.HubServiceBlockingStub stub, Scanner scanner) {
        if (scanner.findInLine("") == null) { 
            System.out.println("ERRO - Faltam argumentos: Station!");
            return;
        }
        if (scanner.hasNext()) {
            try {
                String station = scanner.next();
                BikeRequest request = BikeRequest.newBuilder()
                    .setUserId(_user)
                    .setCoordinates(Coordinates.newBuilder()
					    .setLatitude(_latitude)
					    .setLongitude(_longitude)
					    .build()
				    ).setStationId(station)
				    .build();
                BikeResponse response = stub.bikeDown(request);
                System.out.println("OK");
            } catch (InputMismatchException e) {
                //System.out.println("ERRO - Argumentos incorretos para comando bike-down!");
                System.out.println("ERRO fora de alcance");
                //TO CHECK mensagem de erro que queremos responder e excecao que recebemos
            }
        }
    }
    



    private static void ping(HubServiceGrpc.HubServiceBlockingStub stub) {
        
        PingRequest request = PingRequest.newBuilder().setInput(_user).build();
        debug(request);
        PingResponse response = stub.ping(request);
        System.out.println(response.getOutput());
        
    }

}
