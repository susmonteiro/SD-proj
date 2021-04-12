package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 

import static pt.tecnico.bicloin.hub.HubMain.debug;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.rec.grpc.Rec;

import pt.tecnico.rec.RecordFrontend;

public class Hub {
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

    public Hub(String recIP, int recPORT, Map<String, User> users, Map<String, Station> stations) {
        this.users = users;
        this.stations = stations;
        rec = new RecordFrontend(recIP, recPORT);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Station> getStations() {
        return stations;
    }    

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public void setStations(Map<String, Station> stations) {
        this.stations = stations;
    }

    public void addUser(User user){
        users.put(user.getId(), user);
    }

    public void addStation(Station station) {
        stations.put(station.getId(), station);
    }

    public void initializeRec() {
        // Users lazy loaded (registers only initialized on first access)
        debug("@Hub Initializing Rec...");
        System.out.println("Inside init rec");
		for (String stationId: stations.keySet()) {
            debug("id: " + stationId + "\n" + stations.get(stationId.toString()));
            int nBicycles = stations.get(stationId).getNBicycles();
            
            Rec.WriteRequest request = Rec.WriteRequest.newBuilder()
                .setRegister(Rec.RegisterRequest.newBuilder()
                    .setId(stationId))
                .setData(Rec.RegisterValue.newBuilder()
                    .setRegNBikes(Rec.RegisterNBikes.newBuilder()
                        .setNBikes(nBicycles)))
                .build();
            debug(request);

            rec.write(request);
        }
	}

    public SysStatusResponse getAllServerStatus() {
        // TODO zookeper integration
        SysStatusResponse.Builder serverResponse = SysStatusResponse.newBuilder();
        boolean status = true;
        try {
            Rec.PingRequest request = Rec.PingRequest.newBuilder().setInput("friend").build();
            rec.ping(request);
            
        } catch (Exception e) {
            debug(e);
            status = false;
        } // comms
        
        serverResponse.addServerStatus(SysStatusResponse.StatusResponse.newBuilder()
            .setPath(rec.getPath())
            .setStatus(status)
            .build());

        debug(serverResponse);
        return serverResponse.build();
    }
}
