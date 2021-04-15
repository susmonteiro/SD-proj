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
    private static final int BIC_EXCHANGE_RATE = 10;    /* Bic (Bicloin) is the currency */
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

    public Hub(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations) {
        this.users = users;
        this.stations = stations;
        rec = new RecordFrontend(recIP, recPORT);
    }

    /* Data checks */
    /* =========== */

    public void checkUser(String id) throws InvalidUserException {
        if (!users.containsKey(id)) { throw new InvalidUserException(); }
    }

    public void checkUserPhoneNumber(String id, String phoneNumber) throws InvalidPhoneNumberException {
        if (!users.get(id).getPhoneNumber().equals(phoneNumber)) { throw new InvalidPhoneNumberException(); }
    }

    public void checkValidTopUpAmout(int value) throws InvalidTopUpAmountException {
        if (!(value >= 1 && value <= 20)) { throw new InvalidTopUpAmountException(); }
    }

    /* Methods */
    /* ======= */
    public int getBicFromMoney(int value) {
        return value*BIC_EXCHANGE_RATE;
    }

    public void initializeRec() {
        /* Users lazy loaded (registers only initialized on first access) */
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
        
        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue());
        debug("@Hub #Balance\n**Request:\n" + request);
        
        Rec.ReadResponse response = rec.read(request);
        debug("@Hub #Balance\n**Response:\n" + response);
        
        int value = getBalanceValue(response.getData());
        debug("@Hub #Balance\n**Value:\n" + value);

        return value;
    }

    public int topUp(String id, int value, String phoneNumber) 
        throws StatusRuntimeException, InvalidUserException, 
            InvalidPhoneNumberException, InvalidTopUpAmountException {
        
        checkUser(id);
        checkUserPhoneNumber(id, phoneNumber);
        checkValidTopUpAmout(value);

        int oldBalance = balance(id);
        int newBalance = oldBalance + getBicFromMoney(value);

        Rec.RegisterRequest request = getRegisterRequest(id, getRegisterBalanceAsRegisterValue(newBalance));
        debug("@Hub #TopUp\n**Request:\n" + request);

        rec.write(request);

        return newBalance;
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
