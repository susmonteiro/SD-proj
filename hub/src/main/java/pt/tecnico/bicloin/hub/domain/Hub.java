package pt.tecnico.bicloin.hub.domain;

import java.util.Map; 
import java.util.HashMap; 
import java.util.List;
import java.util.ArrayList;

import pt.tecnico.bicloin.hub.HubMain;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.rec.grpc.Rec;

import io.grpc.StatusRuntimeException;

import pt.tecnico.rec.RecordFrontend;

public class Hub {
    private Map<String, User> users;
    private Map<String, Station> stations;
    private RecordFrontend rec;

    public Hub(String recIP, int recPORT) {
        users = new HashMap<String, User>();
        stations = new HashMap<String, Station>();
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

    public SysStatusResponse getAllServerStatus() {
        // TODO zookeper integration
        SysStatusResponse.Builder serverResponse = SysStatusResponse.newBuilder();
        boolean status = true;
        try {
            Rec.PingRequest request = Rec.PingRequest.newBuilder().setInput("friend").build();
            rec.ping(request);
            
        } catch (Exception e) {
            status = false;
        } // comms
        
        serverResponse.addServerStatus(SysStatusResponse.StatusResponse
            .newBuilder()
            .setPath(rec.path())
            .setStatus(status)
            .build());

        HubMain.debug(serverResponse);
        return serverResponse.build();
    }
}
