package pt.tecnico.bicloin.hub.domain;

import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidFileInputException;

public class Station {
    public static final short NAME_IDX = 0;
    public static final short ID_IDX = 1;
    public static final short LATITUDE_IDX = 2;
    public static final short LONGITUDE_IDX = 3;
    public static final short NDOCKS_IDX = 4;
    public static final short NBICYCLES_IDX = 5;
    public static final short REWARD_IDX = 6;

    private String _name;
    private String _id;          // 4 chars
    private float _latitude;     // between -90 and 90
    private float _longitude;    // between -180 and 180
    private int _nDocks;
    private int _nBicycles;
    private int _reward;

    // TODO permitir que o servidor possa arrancar sem stations

    public Station(String name, String id, float latitude, float longitude, int nDocks,
            int nBicycles, int reward) throws InvalidFileInputException {
        try {
            checkId(id);
            checkLatitude(latitude);
            checkLongitude(longitude);
            checkNBicycles(nBicycles, nDocks);
            checkNDocks(nDocks);
            checkReward(reward);
        } catch (InvalidArgumentException e) {
            throw new InvalidFileInputException(e.getMessage());
        }

        _name = name;
        _id = id;
        _latitude = latitude;
        _longitude = longitude;
        _nDocks = nDocks;
        _nBicycles = nBicycles;
        _reward = reward;
    }

    public Station(String[] fields) throws InvalidFileInputException {
        this(fields[NAME_IDX], fields[ID_IDX], Float.parseFloat(fields[LATITUDE_IDX]),
            Float.parseFloat(fields[LONGITUDE_IDX]), Integer.parseInt(fields[NDOCKS_IDX]), 
            Integer.parseInt(fields[NBICYCLES_IDX]), Integer.parseInt(fields[REWARD_IDX]));
    }

    /* Data checks */
    /* =========== */
    
    public static void checkId(String id) throws InvalidArgumentException {
        if (id.length() != 4)  throw new InvalidArgumentException("ID " + id + " is invalid.\nID needs to have 4 chars.");
    }
    public static void checkLatitude(float latitude) throws InvalidArgumentException {
        if (latitude < -90 || latitude > 90) throw new InvalidArgumentException("Latitude " + latitude + " is invalid\n.Latitude has to be between -90 and 90.");
    }
    public static void checkLongitude(float longitude) throws InvalidArgumentException {
        if (longitude < -180 || longitude > 180) throw new InvalidArgumentException("Longitude " + longitude + " is invalid\n.Longitude has to be between -180 and 180.");
    }
    public static void checkNDocks(int nDocks) throws InvalidArgumentException {
        if (nDocks < 0) throw new InvalidArgumentException("Number of Docks " + nDocks + " is invalid.\nNumber of Docks cannot be negative.");    
    }
    public static void checkNBicycles(int nBicycles, int nDocks) throws InvalidArgumentException {
        if (nBicycles < 0 || nBicycles > nDocks) throw new InvalidArgumentException("Number of Bicycles " + nBicycles + " is invalid.\nNumber of Bicycles cannot be negative or higher than the Number of Docks.");
    }
    public static void checkReward(int reward) throws InvalidArgumentException {
        if (reward < 0) throw new InvalidArgumentException("Reward " + reward + " is invalid.\nReward has to be positive.");
    }
        


    public String getName() { return _name; }
    
    public String getId() { return _id; }
    
    public float getLat() { return _latitude; }
    
    public float getLong() { return _longitude; }
    
    public int getNDocks() { return _nDocks; }

    public int getNBicycles() { return _nBicycles; }
        
    public int getReward() { return _reward; }
    
    public void setName(String name) { this._name = name; }

    public void setId(String id) { this._id = id; }
    
    public void setLat(float latitude) { this._latitude = latitude; }
    
    public void setLong(float longitude) { this._longitude = longitude; }
    
    public void setNDocks(int nDocks) { this._nDocks = nDocks; }
    
    public void setNBicycles(int nBicycles) { this._nBicycles = nBicycles; }
    
    public void setReward(int reward) { this._reward = reward; }

    public String toString() {
        return "Name: " + _name + "\n" +
            "Latitude: " + _latitude + "\n" +
            "Longitude: " + _longitude + "\n" +
            "Number Docks: " + _nDocks + "\n" +
            "Number Bicycles: " + _nBicycles + "\n" +
            "Reward: " + _reward;
    }

}
