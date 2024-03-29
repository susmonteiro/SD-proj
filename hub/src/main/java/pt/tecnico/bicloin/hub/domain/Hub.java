package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import pt.tecnico.bicloin.hub.HubMain;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.frontend.RecordFrontend;
import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;
import static pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.*;

import pt.tecnico.bicloin.hub.domain.exception.*;
import pt.tecnico.bicloin.hub.frontend.HubFrontend;
import io.grpc.StatusRuntimeException;

public class Hub {
    private static Debug DEBUG = Debug.NO_DEBUG;

    private static final int BIC_EXCHANGE_RATE = 10;        /* Bic (Bicloin) is the currency */
    private static final int BIKE_UP_PRICE = 10;            /* Price of bike up in project currency (Bic`s) */
    private static final int MIN_DIST_FOR_BIKE_UP = 200;    /* Minimun distance to the station to be able to request a bike */
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontendReplicationWrapper rec;

    private static final int EARTH_RADIUS = 6371;

    public Hub(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations) {
        this.users = users;
        this.stations = stations;
        rec = new RecordFrontendReplicationWrapper(zooHost, zooPort, instance_num);
    }
    
    public Hub(String zooHost, int zooPort, int instance_num, Map<String, User> users, Map<String, Station> stations, Debug debug) {
        DEBUG = debug;

        this.users = users;
        this.stations = stations;
        rec = new RecordFrontendReplicationWrapper(zooHost, zooPort, instance_num, debug);
    }

    public void shutdown() {
        rec.close();
    }

    /* Methods */
    /* ======= */
    
    public void initializeRec() {
        try {
            debug("Initializing Rec...");
            debug("Users:");
            for (String id : users.keySet()) {
                debug("id: " + id + "\n" + users.get(id));
                rec.setBalance(id);
                rec.setOnBike(id);
            }

            debug("Stations:");
            for (String id: stations.keySet()) {
                debug("id: " + id + "\n" + stations.get(id));
                int nBicycles = stations.get(id).getNBicycles();
                
                rec.setNBikes(id, nBicycles);
                rec.setNPickUps(id);
                rec.setNDeliveries(id);
            }
        } catch (StatusRuntimeException e) {
			System.err.println("Could not initialize rec. Rec is down.");
			debug("Got exception:" + e.getStatus().getDescription());
		}
	}
    
    public AmountResponse balance(String id) throws StatusRuntimeException, InvalidUserException {
        checkUser(id);
    
        synchronized(users.get(id)) {           // prevent read of concurrent write
            return AmountResponse.newBuilder()
                .setBalance(rec.getBalance(id))
                .build();
        }
    }

    public AmountResponse topUp(String id, int value, String phoneNumber) 
        throws StatusRuntimeException, InvalidArgumentException {
        
        checkUser(id);
        checkUserPhoneNumber(id, phoneNumber);
        checkValidTopUpAmout(value);
        
        int newBalance;

        // only synchronize the user
        synchronized (users.get(id)) {
            int oldBalance = rec.getBalance(id);
            newBalance = oldBalance + getBicFromMoney(value);

            rec.setBalance(id, newBalance);
        }

        return AmountResponse.newBuilder()
            .setBalance(newBalance)
            .build();
    }

    public InfoStationResponse infoStation(String stationId) 
        throws StatusRuntimeException, InvalidArgumentException {
        
        checkStation(stationId);

        Station station = stations.get(stationId);
        String name = station.getName();
        float lat = station.getLat();
        float lon = station.getLong();
        int nDocks = station.getNDocks();
        int reward = station.getReward();

        int availableBikes;
        int nPickUps;
        int nDeliveries;

        synchronized(stations.get(stationId)) {     // prevent read of concurrent write
            availableBikes = rec.getNBikes(stationId);
            nPickUps = rec.getNPickUps(stationId); 
            nDeliveries = rec.getNDeliveries(stationId); 
        }

        InfoStationResponse response = InfoStationResponse.newBuilder()
            .setCoordinates(Coordinates.newBuilder()
                .setLatitude(lat)
                .setLongitude(lon)
                .build()
            ).setName(name)
            .setNDocks(nDocks)
            .setReward(reward)
            .setNBicycles(availableBikes)
            .setNPickUps(nPickUps)
            .setNDeliveries(nDeliveries)
            .build();
        
        debug(response);
        
        return response;

    }

    public LocateStationResponse locateStation (float latitude, float longitude, int count)
        throws StatusRuntimeException, InvalidArgumentException {
        
        User.checkLatitude(latitude);
        User.checkLongitude(longitude);
        checkCount(count);
        
        Map<Double, String> allDistances = new HashMap<Double, String>();
		List<Double> lowestDistances = new ArrayList<Double>();

		for (String stationId: stations.keySet()) {
			float lat = stations.get(stationId).getLat();
			float lon = stations.get(stationId).getLong();
			double dist = distance(latitude, longitude, lat, lon);
            allDistances.put(dist, stationId);
			lowestDistances.add(dist);
		}
		
        lowestDistances.sort(Comparator.naturalOrder());
		
        int n = (count < lowestDistances.size()) ? count : lowestDistances.size();

		LocateStationResponse.Builder response = LocateStationResponse.newBuilder();
		for (int i=0; i<n; i++) {
			String name = allDistances.get(lowestDistances.get(i));
            //if count > stations only send those that exist
			response.addStationId(name);
			debug(name);
		}
		return response.build();
    }

    public BikeResponse bikeUp(String userId, float latitude, float longitude, String stationId)
            throws StatusRuntimeException, InvalidArgumentException, FailedPreconditionException {
        
        checkUser(userId);
        Station.checkLatitude(latitude);
        Station.checkLongitude(longitude);
        checkStation(stationId);
        checkLocationAllowed(stationId, latitude, longitude);

        /* Always synchronize in this order to avoid DeadLocks */
        synchronized (users.get(userId)) {
            // check if already onBike and has enough money
            int balance = rec.getBalance(userId);
            checkUserCanBikeUp(userId, balance);

            synchronized (stations.get(stationId)) {
                // check if bikes available
                int availableBikes = rec.getNBikes(stationId);
                checkStationAvailableBikes(availableBikes);

                // decrease bikes available
                rec.setNBikes(stationId, availableBikes-1);

                // increase bike pickups
                int newNPickUps = 1 + rec.getNPickUps(stationId);
                rec.setNPickUps(stationId, newNPickUps);
                debug("increasing nPickUps to: " + newNPickUps);
            }
            
            // decrease money and change status onBike
            rec.setBalance(userId, balance-BIKE_UP_PRICE);
            rec.setOnBike(userId, true);
        }

        return BikeResponse.getDefaultInstance();
    }

    public BikeResponse bikeDown(String userId, float latitude, float longitude, String stationId)
            throws StatusRuntimeException, InvalidArgumentException, FailedPreconditionException {
        
        checkUser(userId);
        Station.checkLatitude(latitude);
        Station.checkLongitude(longitude);
        checkStation(stationId);
        checkLocationAllowed(stationId, latitude, longitude);

        /* Always synchronize in this order to avoid DeadLocks */
        synchronized (users.get(userId)) {
            // check if not onBike
            checkUserNotOnBike(userId);

            synchronized (stations.get(stationId)) {
                // check if docks available
                int dockedBikes = rec.getNBikes(stationId);
                checkStationAvailableDocks(stationId, dockedBikes);

                // increase bikes available
                rec.setNBikes(stationId, dockedBikes+1);

                // increse bike deliveries
                int newNDeliveries = 1 + rec.getNDeliveries(stationId);
                rec.setNDeliveries(stationId, newNDeliveries); 
                debug("new nDeliveries: " + newNDeliveries);
            }

            // get reward money and change status onBike
            int reward = stations.get(stationId).getReward();
            int balance = rec.getBalance(userId);
            rec.setBalance(userId, balance+reward);

            rec.setOnBike(userId, false);
        }

        return BikeResponse.getDefaultInstance();
    }

    public PingResponse ping(String input) throws InvalidArgumentException {
		checkEmptyInput(input);

		String output = "Hello " + input + "! " + HubMain.identity();
        debug("#ping\tResponse: " + output);

		return PingResponse.newBuilder().setOutput(output).build();
    }

    public SysStatusResponse getAllServerStatus() {
        SysStatusResponse.Builder serverResponse = SysStatusResponse.newBuilder();
        // Hub (Self)
        boolean status = true;
        try {
            ping("Myself");

        } catch (StatusRuntimeException e) {
            debug("#GetAllServerStatus:\nCaught exception with description: " +
                e.getStatus().getDescription());
            status = false;     // log on error
        } catch (InvalidArgumentException e) { 
            // This should not happen, local function call
            debug("#GetAllServerStatus:\nCaught exception with description: " +
                e.getMessage());
        }

        serverResponse.addServerStatus(SysStatusResponse.StatusResponse.newBuilder()
                .setPath(HubMain.getPath())
                .setStatus(status)
                .build());

        // Rec
        rec.getSysStatus().forEach((rPath, rStatus) -> serverResponse.addServerStatus(
            SysStatusResponse.StatusResponse.newBuilder()
                .setPath(rPath)
                .setStatus(rStatus)
                .build()
            )
        );

        debug(serverResponse);
        return serverResponse.build();
    }

    /* Auxiliar */
    /* ======== */

    public int getBicFromMoney(int value) {
        return value*BIC_EXCHANGE_RATE;
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
		return distance; 
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }


    /* Data checks */
    /* =========== */

    public void checkUser(String id) throws InvalidUserException {
        if (!users.containsKey(id)) { throw new InvalidUserException(); }
    }
    
    public void checkStation(String id) throws InvalidStationException {
        if (!stations.containsKey(id)) { throw new InvalidStationException(); }
    }

    public void checkUserPhoneNumber(String id, String phoneNumber) throws InvalidPhoneNumberException {
        if (!users.get(id).getPhoneNumber().equals(phoneNumber)) { throw new InvalidPhoneNumberException(); }
    }

    public void checkValidTopUpAmout(int value) throws InvalidTopUpAmountException {
        if (!(value >= 1 && value <= 20)) { throw new InvalidTopUpAmountException(); }
    }

    public void checkUserCanBikeUp(String id, int money) throws FailedPreconditionException {
        /* Use only with trusted id */
        checkUserOnBike(id);

        // checkUserHasMoney
        if (money < BIKE_UP_PRICE) throw new NotEnoughMoneyException();
}

    public void checkUserOnBike(String id) throws StatusRuntimeException, UserAlreadyOnBikeException {
        /* Use only with trusted id */
        boolean onBike = rec.getOnBike(id);
        if (onBike) throw new UserAlreadyOnBikeException();
    }

    public void checkUserNotOnBike(String id) throws StatusRuntimeException, UserNotOnBikeException {
        /* Use only with trusted id */
        boolean onBike = rec.getOnBike(id);
        if (!onBike) throw new UserNotOnBikeException();
    }

    /* Implemented in function for future conditions (eg. bike reserve) */
    public void checkStationAvailableBikes(int value) throws NoBikeAvailableException {
        if (value <= 0) throw new NoBikeAvailableException();
    }

    public void checkStationAvailableDocks(String id, int value) throws NoDocksAvailableException {
        /* Use only with trusted id */
        if (value >= stations.get(id).getNDocks()) throw new NoDocksAvailableException();
    }

    public void checkLocationAllowed(String stationId, float lat, float lon) throws UserTooFarAwayFromStationException {
        /* Use only with trusted id */
        Station station = stations.get(stationId); 
        if (distance(lat, lon, station.getLat(), station.getLong()) > MIN_DIST_FOR_BIKE_UP)
            throw new UserTooFarAwayFromStationException();
    }

    public void checkCount(int count) throws InvalidArgumentException {
        if (count < 0) throw new InvalidStationCountException();
    }

    public void checkEmptyInput(String input) throws EmptyInputException {
        if (input == null || input.isBlank()) throw new EmptyInputException();
    }


    /** Helper method to print debug messages. */
	public void debug(Object debugMessage) {
		if (DEBUG == Debug.STRONGER_DEBUG)
			System.err.println(debugMessage);
	}

    public void debugDemo(Object debugMessage) {
		if (DEBUG == Debug.STRONGER_DEBUG || DEBUG == Debug.WEAKER_DEBUG)
			System.err.println(debugMessage);
	}	
}