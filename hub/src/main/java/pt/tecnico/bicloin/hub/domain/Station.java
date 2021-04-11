package pt.tecnico.bicloin.hub.domain;

public class Station {
    private String _name;
    private String _id;          // 4 chars
    private float _latitude;     // between -90 and 90
    private float _longitude;    // between -180 and 180
    private int _nDocks;
    private int _nBicycles;    // only in rec?
    private int _reward;

    // TODO permitir que o servidor possa arrancar sem stations

    public Station(String name, String id, float latitude, float longitude, int nDocks, int nBicycles, int reward) {
            _name = name;
            _id = id;
            _latitude = latitude;
            _longitude = longitude;
            _nDocks = nDocks;
            _nBicycles = nBicycles;
            _reward = reward;
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
    
    public void setNBicycles(int nBicycles) { this._nBicycles = nBicycles; }
    
    public void setNDocks(int nDocks) { this._nDocks = nDocks; }
    
    public void setReward(int reward) { this._reward = reward; }

}
