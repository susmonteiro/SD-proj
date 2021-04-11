package pt.tecnico.domain;

public class Station {
    private String _name;
    private String _id;     // 4 chars
    private float _latitude;     // between -90 and 90
    private float _longitude;    // between -180 and 180
    private int _numDocks;
    private int _numBicycles;       // only in rec?
    private int _reward;

    // TODO permitir que o servidor possa arrancar sem stations


    public String getName() { return _name; }
    
    public String getId() { return _id; }
    
    public float getLat() { return _latitude; }
    
    public float getLong() { return _longitude; }
    
    public int getNumDocks() { return _numDocks; }
    
    public int getNumBicycles() { return _numBicycles; }
    
    public int getReward() { return _reward; }
    
    public void setName(String name) { this._name = name; }

    public void setId(String id) { this._id = id; }
    
    public void setLat(float latitude) { this._latitude = latitude; }
    
    public void setLong(float longitude) { this._longitude = longitude; }
    
    public void set_numBicycles(int numBicycles) { this._numBicycles = numBicycles; }
    
    public void set_numDocks(int numDocks) { this._numDocks = numDocks; }
    
    public void set_reward(int reward) { this._reward = reward; }


}
