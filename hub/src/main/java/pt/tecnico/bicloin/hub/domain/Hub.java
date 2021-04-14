package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 

import static pt.tecnico.bicloin.hub.HubMain.debug;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.frontend.RecordFrontend;

import pt.tecnico.bicloin.hub.domain.exception.*;
import io.grpc.StatusRuntimeException;

class Default {
    /* Normaly this will be the same as GRPC defaults
     *  but was implemented this way to be 
     *  independent from the library */
    public static final int BALANCE = 0;
    public static final boolean ON_BIKE = false;
    public static final int N_BIKES = 0;
    public static final int N_PICK_UPS = 0;
    public static final int N_DELIVERIES = 0;
}

public class Hub {
    private static final int BIKE_UP_PRICE = 10;    /* Price of bike up in project currency (Bic`s) */ 
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

    public Hub(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations) {
        this.users = users;
        this.stations = stations;
        rec = new RecordFrontend(recIP, recPORT);
    }

    /* Methods */
    /* ======= */

    public void initializeRec() {
        // Users lazy loaded (registers only initialized on first access)
        debug("@Hub Initializing Rec...");
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
    
        return getUserBalance(id);
    }

    public synchronized int topUp(String id, int value, String phoneNumber) 
        throws StatusRuntimeException, InvalidArgumentException {
        
        checkUser(id);
        checkUserPhoneNumber(id, phoneNumber);
        checkValidTopUpAmout(value);

        int oldBalance = getUserBalance(id);
        int newBalance = oldBalance + value;

        setUserBalance(id, newBalance);

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
            int balance = getUserBalance(userId);
            checkUserCanBikeUp(userId, balance);

            synchronized (stations.get(stationId)) {
                // check if bikes available
                int availableBikes = getStationAvailableBikes(stationId);
                checkStationAvailableBikes(availableBikes);

                // perform bikup - Station
                    // decrease bikes available
                setStationAvailableBikes(stationId, availableBikes-1);
            }
            // perform bikup - User
                // decrease money
                // change status onBike
            setUserBalance(userId, balance-BIKE_UP_PRICE);
            setUserOnBike(userId, true);
        }
    }

    public SysStatusResponse getAllServerStatus() {
        // TODO zookeper integration
        SysStatusResponse.Builder serverResponse = SysStatusResponse.newBuilder();
        boolean status = true;
        try {
            Rec.PingRequest request = getPingRequest("friend");
            rec.ping(request);

        } catch (StatusRuntimeException e) {
            debug("@Hub #GetAllServerStatus:\nCaught exception with description: " +
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

    /* Record Getters and Setters */
    /* ========================== */

    public int getUserBalance(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue());
        debug("@Hub #getUserBalance\n**Request:\n" + request);
        
        Rec.ReadResponse response = rec.read(request);
        debug("@Hub #getUserBalance\n**Response:\n" + response);
        
        int value = getBalanceValue(response.getData());
        debug("@Hub #getUserBalance\n**Value:\n" + value);

        return value;
    }
    public void setUserBalance(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue(value));
        debug("@Hub #setStationAvailableBikes\n**Request:\n" + request);

        rec.write(request);
    }
    

    public int getStationAvailableBikes(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue());
        debug("@Hub #getAvailableBikes\n**Request:\n" + request);
        
        Rec.ReadResponse response = rec.read(request);
        debug("@Hub #getAvailableBikes\n**Response:\n" + response);
        
        int value = getNBikesValue(response.getData());
        debug("@Hub #getAvailableBikes\n**Value:\n" + value);

        return value;
    }
    public void setStationAvailableBikes(String id, int value) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterNBikesAsRegisterValue(value));
        debug("@Hub #setStationAvailableBikes\n**Request:\n" + request);
        
        rec.write(request);
    }

    public boolean getUserOnBike(String id) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue());
        debug("@Hub #getUserOnBike\n**Request:\n" + request);
        
        Rec.ReadResponse response = rec.read(request);
        debug("@Hub #getUserOnBike\n**Response:\n" + response);
        
        boolean value = getOnBikeValue(response.getData());
        debug("@Hub #getUserOnBike\n**Value:\n" + value);

        return value;
    }
    public void setUserOnBike(String id, Boolean value) throws StatusRuntimeException {
        /* Use only with trusted id */
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue(value));
        debug("@Hub #setUserOnBike\n**Request:\n" + request);
        
        rec.write(request);
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
        Rec.RegisterRequest onBikeReq = getRegisterRequest(id, getRegisterOnBikeAsRegisterValue());
        Rec.ReadResponse onBikeResp = rec.read(onBikeReq);

        boolean onBike = getOnBikeValue(onBikeResp.getData());
        if (onBike) throw new UserAlreadyOnBikeException();
    }

    /* Implemented in function for future conditions (eg. bike reserve) */
    public void checkStationAvailableBikes(int value) throws NoBikeAvailableException {
        if (value <= 0) throw new NoBikeAvailableException();
    }


	/* Record Communication */
    /* ==================== */

        /* Message analysis */
        /* ++++++++++++++++ */

    private int getBalanceValue(Rec.RegisterValue response) {
        return response.hasRegBalance() ?
            response.getRegBalance().getBalance() : Default.BALANCE;
    }

    private boolean getOnBikeValue(Rec.RegisterValue response) {
        return response.hasRegOnBike() ?
            response.getRegOnBike().getOnBike() : Default.ON_BIKE;
    }

    private int getNBikesValue(Rec.RegisterValue response) {
        return response.hasRegNBikes() ?
            response.getRegNBikes().getNBikes() : Default.N_BIKES;
    }

    private int getNPickUpsValue(Rec.RegisterValue response) {
        return response.hasRegNPickUps() ?
            response.getRegNPickUps().getNPickUps() : Default.N_PICK_UPS;
    }

    private int getNDeliveriesValue(Rec.RegisterValue response) {
        return response.hasRegNDeliveries() ?
            response.getRegNDeliveries().getNDeliveries() : Default.N_DELIVERIES;
    }


        /* Message building */
        /* ++++++++++++++++ */

	private static Rec.PingRequest getPingRequest(String input) {
		return Rec.PingRequest.newBuilder().setInput(input).build();
	}

    private static Rec.RegisterRequest getRegisterRequest(String id, Rec.RegisterValue value) {
        return Rec.RegisterRequest.newBuilder()
                .setId(id)
                .setData(value)
                .build();
    }

	private static Rec.RegisterValue getRegisterBalanceAsRegisterValue(int value) {
		return Rec.RegisterValue.newBuilder().setRegBalance(
				Rec.RegisterBalance.newBuilder().setBalance(value).build()
			).build();
	}
	private static Rec.RegisterValue getRegisterBalanceAsRegisterValue() {
		return Rec.RegisterValue.newBuilder().setRegBalance(
				Rec.RegisterBalance.getDefaultInstance()
			).build();
	}

	private static Rec.RegisterValue getRegisterOnBikeAsRegisterValue(boolean value) {
		return Rec.RegisterValue.newBuilder().setRegOnBike(
				Rec.RegisterOnBike.newBuilder().setOnBike(value).build()
			).build();
	}
	private static Rec.RegisterValue getRegisterOnBikeAsRegisterValue() {
		return Rec.RegisterValue.newBuilder().setRegOnBike(
				Rec.RegisterOnBike.getDefaultInstance()
			).build();
	}
	
	private static Rec.RegisterValue getRegisterNBikesAsRegisterValue(int value) {
		return Rec.RegisterValue.newBuilder().setRegNBikes(
				Rec.RegisterNBikes.newBuilder().setNBikes(value).build()
			).build();
	}
	private static Rec.RegisterValue getRegisterNBikesAsRegisterValue() {
		return Rec.RegisterValue.newBuilder().setRegNBikes(
				Rec.RegisterNBikes.getDefaultInstance()
			).build();
	}

	private static Rec.RegisterValue getRegisterNPickUpsAsRegisterValue(int value) {
		return Rec.RegisterValue.newBuilder().setRegNPickUps(
				Rec.RegisterNPickUps.newBuilder().setNPickUps(value).build()
			).build();
	}
	private static Rec.RegisterValue getRegisterNPickUpsAsRegisterValue() {
		return Rec.RegisterValue.newBuilder().setRegNPickUps(
				Rec.RegisterNPickUps.getDefaultInstance()
			).build();
	}

	private static Rec.RegisterValue getRegisterNDeliveriesAsRegisterValue(int value) {
		return Rec.RegisterValue.newBuilder().setRegNDeliveries(
				Rec.RegisterNDeliveries.newBuilder().setNDeliveries(value).build()
			).build();
	}
	private static Rec.RegisterValue getRegisterNDeliveriesAsRegisterValue() {
		return Rec.RegisterValue.newBuilder().setRegNDeliveries(
				Rec.RegisterNDeliveries.getDefaultInstance()
			).build();
	}
}
