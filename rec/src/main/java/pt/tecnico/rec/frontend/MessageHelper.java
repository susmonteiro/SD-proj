package pt.tecnico.rec.frontend;

import pt.tecnico.rec.grpc.Rec.*;

public class MessageHelper {

	/* Message analysis */
	/* ++++++++++++++++ */

	private class Default {
		/* Normaly this will be the same as GRPC defaults
		 *  but was implemented this way to be 
		 *  independent from the library */
		public static final int BALANCE = 0;
		public static final boolean ON_BIKE = false;
		public static final int N_BIKES = 0;
		public static final int N_PICK_UPS = 0;
		public static final int N_DELIVERIES = 0;
	}

    public static int getBalanceValue(RegisterValue response) {
        return response.hasRegBalance() ?
            response.getRegBalance().getBalance() : Default.BALANCE;
    }

	public static int getBalanceDefaultValue() {
		return Default.BALANCE;
	}

    public static boolean getOnBikeValue(RegisterValue response) {
        return response.hasRegOnBike() ?
            response.getRegOnBike().getOnBike() : Default.ON_BIKE;
    }

	public static boolean getOnBikeDefaultValue() {
		return Default.ON_BIKE;
	}

    public static int getNBikesValue(RegisterValue response) {
        return response.hasRegNBikes() ?
            response.getRegNBikes().getNBikes() : Default.N_BIKES;
    }

	public static int getNBikesDefaultValue() {
		return Default.N_BIKES;
	}

    public static int getNPickUpsValue(RegisterValue response) {
        return response.hasRegNPickUps() ?
            response.getRegNPickUps().getNPickUps() : Default.N_PICK_UPS;
    }

	public static int getNPickUpsDefaultValue() {
		return Default.N_PICK_UPS;
	}

    public static int getNDeliveriesValue(RegisterValue response) {
        return response.hasRegNDeliveries() ?
            response.getRegNDeliveries().getNDeliveries() : Default.N_DELIVERIES;
    }

	public static int getNDeliveriesDefaultValue() {
		return Default.N_DELIVERIES;
	}
	
	/* Message Building */
	/* ++++++++++++++++ */

    public static PingRequest getPingRequest(String input) {
		return PingRequest.newBuilder().setInput(input).build();
	}

    public static RegisterRequest getRegisterRequest(String id, RegisterValue value) {
        return RegisterRequest.newBuilder()
                .setId(id)
                .setData(value)
                .build();
    }

	public static RegisterValue getRegisterBalanceAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegBalance(
				RegisterBalance.newBuilder().setBalance(value).build()
			).build();
	}
	public static RegisterValue getRegisterBalanceAsRegisterValue() {
		return RegisterValue.newBuilder().setRegBalance(
				RegisterBalance.getDefaultInstance()
			).build();
	}

	public static RegisterValue getRegisterOnBikeAsRegisterValue(boolean value) {
		return RegisterValue.newBuilder().setRegOnBike(
				RegisterOnBike.newBuilder().setOnBike(value).build()
			).build();
	}
	public static RegisterValue getRegisterOnBikeAsRegisterValue() {
		return RegisterValue.newBuilder().setRegOnBike(
				RegisterOnBike.getDefaultInstance()
			).build();
	}
	
	public static RegisterValue getRegisterNBikesAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNBikes(
				RegisterNBikes.newBuilder().setNBikes(value).build()
			).build();
	}
	public static RegisterValue getRegisterNBikesAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNBikes(
				RegisterNBikes.getDefaultInstance()
			).build();
	}

	public static RegisterValue getRegisterNPickUpsAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNPickUps(
				RegisterNPickUps.newBuilder().setNPickUps(value).build()
			).build();
	}
	public static RegisterValue getRegisterNPickUpsAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNPickUps(
				RegisterNPickUps.getDefaultInstance()
			).build();
	}

	public static RegisterValue getRegisterNDeliveriesAsRegisterValue(int value) {
		return RegisterValue.newBuilder().setRegNDeliveries(
				RegisterNDeliveries.newBuilder().setNDeliveries(value).build()
			).build();
	}
	public static RegisterValue getRegisterNDeliveriesAsRegisterValue() {
		return RegisterValue.newBuilder().setRegNDeliveries(
				RegisterNDeliveries.getDefaultInstance()
			).build();
	}
    
}
