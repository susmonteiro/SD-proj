package pt.tecnico.bicloin.hub.frontend;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class MessageHelper {
    public static BalanceRequest getBalanceRequest(String userId) {
        return BalanceRequest.newBuilder().setUserId(userId).build();
    }

    public static TopUpRequest getTopUpRequest(String userId, int amount, String phoneNumber) {
        return TopUpRequest.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setPhoneNumber(phoneNumber)
                .build();
            
    }

    public static InfoStationRequest getInfoStationRequest(String stationId) {
        return InfoStationRequest.newBuilder()
                .setStationId(stationId)
                .build();
    }

    public static LocateStationRequest getLocateStationRequest(float latitude, float longitude, int nStations) {
        return LocateStationRequest.newBuilder()
                .setCoordinates(Coordinates.newBuilder()
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .build()
                ).setNStations(nStations)
                .build();
    }

    public static BikeRequest getBikeRequest(String userId, float latitude, float longitude, String stationId) {
        return BikeRequest.newBuilder()
                .setUserId(userId)
                .setCoordinates(Coordinates.newBuilder()
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .build()
                ).setStationId(stationId)
                .build();
    }

    public static PingRequest getPingRequest(String input) {
        return PingRequest.newBuilder().setInput(input).build();
    }

    public static SysStatusRequest getSysStatusRequest() {
        return SysStatusRequest.newBuilder().build();
    }
}
