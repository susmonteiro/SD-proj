package pt.tecnico.rec.domain;

import java.util.Map; 
import java.util.HashMap; 

public class Rec {
    private Map<String, User> users;
    private Map<String, Station> stations;

    public Rec() {
        users = new HashMap<String, User>();
        stations = new HashMap<String, Station>();
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

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public void addStation(Station station) {
        stations.put(station.getId(), station);
    }
}
