package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.frontend.RecordFrontend;

import pt.tecnico.bicloin.hub.domain.exception.*;
import io.grpc.StatusRuntimeException;

public class Hub {
    private boolean DEBUG = false;

    private static final int BIC_EXCHANGE_RATE = 10;    /* Bic (Bicloin) is the currency */
    private static final int BIKE_UP_PRICE = 10;    /* Price of bike up in project currency (Bic`s) */ 
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

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
    
    public int balance(String id) throws StatusRuntimeException, InvalidUserException {
        checkUser(id);
    
        return rec.getBalance(id);
    }

    public synchronized int topUp(String id, int value, String phoneNumber) 
        throws StatusRuntimeException, InvalidArgumentException {
        
        checkUser(id);
        checkUserPhoneNumber(id, phoneNumber);
        checkValidTopUpAmout(value);

        int oldBalance = rec.getBalance(id);
        int newBalance = oldBalance + getBicFromMoney(value);

        rec.setBalance(id, newBalance);

        return newBalance;
    }

    public void bikeUp(String userId, float latitude, float longitude, String stationId)
            throws StatusRuntimeException, InvalidArgumentException, FailedPreconditionException {
        
        checkUser(userId);
        Station.checkLatitude(latitude);
        Station.checkLongitude(longitude);
        checkStation(stationId);
        // checkLocation(stationId, latitude, longitude); //TODO

        /* Always synchronize in this order to avoid DeadLocks */
        synchronized (users.get(userId)) {
            // check if already onBike and has enought money
            int balance = rec.getBalance(userId);
            checkUserCanBikeUp(userId, balance);

            synchronized (stations.get(stationId)) {
                // check if bikes available
                int availableBikes = rec.getNBikes(stationId);
                checkStationAvailableBikes(availableBikes);

                // perform bikup - Station
                    // decrease bikes available
                rec.setNBikes(stationId, availableBikes-1);
            }
            // perform bikup - User
                // decrease money
                // change status onBike
            rec.setBalance(userId, balance-BIKE_UP_PRICE);
            rec.setOnBike(userId, true);
        }
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

    /* Implemented in function for future conditions (eg. bike reserve) */
    public void checkStationAvailableBikes(int value) throws NoBikeAvailableException {
        if (value <= 0) throw new NoBikeAvailableException();
    }


    /** Helper method to print debug messages. */
	private void debug(Object debugMessage) {
		if (DEBUG)
			System.err.println("@Hub\t" + debugMessage);
	}
}