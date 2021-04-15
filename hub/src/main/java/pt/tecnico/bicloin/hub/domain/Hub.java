package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.frontend.RecordFrontend;

import pt.tecnico.bicloin.hub.domain.exception.*;
import io.grpc.StatusRuntimeException;

public class Hub {
    private boolean DEBUG = false;

    private static final int BIC_EXCHANGE_RATE = 10;        /* Bic (Bicloin) is the currency */
    private static final int BIKE_UP_PRICE = 10;            /* Price of bike up in project currency (Bic`s) */
    private static final int MIN_DIST_FOR_BIKE_UP = 200;    /* Minimun distance to the station to be able to request a bike */
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

    private static final int EARTH_RADIUS = 6371;

    public Hub(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations) {
        this.users = users;
        this.stations = stations;
        rec = new RecordFrontend(recIP, recPORT);
    }
    
    public Hub(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations, boolean debug) {
        DEBUG = debug;

        this.users = users;
        this.stations = stations;
        rec = new RecordFrontend(recIP, recPORT, DEBUG);
    }

    /* Methods */
    /* ======= */
    
    public void initializeRec() {
        /* Users lazy loaded (registers only initialized on first access) */
        debug("Initializing Rec...");
		for (String stationId: stations.keySet()) {
            debug("id: " + stationId + "\n" + stations.get(stationId.toString()));
            int nBicycles = stations.get(stationId).getNBicycles();
            
            Rec.RegisterRequest request = Rec.RegisterRequest.newBuilder()
                .setId(stationId)
                .setData(Rec.RegisterValue.newBuilder()
                    .setRegNBikes(Rec.RegisterNBikes.newBuilder()
                        .setNBikes(nBicycles)))
                .build();
            debug(request);

            rec.write(request);
        }
	}
    
    public AmountResponse balance(String id) throws StatusRuntimeException, InvalidUserException {
        checkUser(id);
    
        return AmountResponse.newBuilder()
            .setBalance(rec.getBalance(id))
            .build();
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

        int availableBikes = rec.getNBikes(stationId);
        int nPickUps = rec.getNPickUps(stationId); 
        int nDeliveries = rec.getNDeliveries(stationId); 

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

		debug(lowestDistances);
		
		LocateStationResponse.Builder response = LocateStationResponse.newBuilder();
		for (int i=0; i<count; i++) {
			String name = allDistances.get(lowestDistances.get(i));
			response.addStationId(name);
			debug(name);
		}
		debug(allDistances);
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
            // check if already onBike and has enought money
            int balance = rec.getBalance(userId);
            checkUserCanBikeUp(userId, balance);

            synchronized (stations.get(stationId)) {
                // check if bikes available
                int availableBikes = rec.getNBikes(stationId);
                checkStationAvailableBikes(availableBikes);

                // decrease bikes available
                rec.setNBikes(stationId, availableBikes-1);
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
            }

            // get reward money and change status onBike
            int reward = stations.get(stationId).getReward();
            int balance = rec.getBalance(userId);
            rec.setBalance(userId, balance+reward);

            rec.setOnBike(userId, false);
        }

        return BikeResponse.getDefaultInstance();
    }

    public SysStatusResponse getAllServerStatus() {
        // TODO zookeper integration
        SysStatusResponse.Builder serverResponse = SysStatusResponse.newBuilder();
        boolean status = true;
        try {
            Rec.PingRequest request = RecordFrontend.getPingRequest("friend");
            rec.ping(request);

        } catch (StatusRuntimeException e) {
            debug("#GetAllServerStatus:\nCaught exception with description: " +
                e.getStatus().getDescription());
            status = false;
        } // comms
        
        serverResponse.addServerStatus(SysStatusResponse.StatusResponse.newBuilder()
            .setPath(rec.getPath())
            .setStatus(status)
            .build());

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
        if (value >= stations.get(id).getNDocks()) throw new NoDocksAvailableException();
    }

    public void checkLocationAllowed(String stationId, float lat, float lon) throws UserTooFarAwayFromStationException {
        /* Use only with trusted id */
        Station station = stations.get(stationId); 
        if (distance(lat, lon, station.getLat(), station.getLong()) > MIN_DIST_FOR_BIKE_UP)
            throw new UserTooFarAwayFromStationException();
    }

    /* ======================================== */

    /** Helper method to print debug messages. */
	private void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@Hub\t" + debugMessage);
	}
}