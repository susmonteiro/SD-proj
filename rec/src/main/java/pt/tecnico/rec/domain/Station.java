package pt.tecnico.rec.domain;

public class Station {
    private String _id;
    private int _nAvailable;
    private int _nPickUps = 0;
    private int _nDeliveries = 0;

    public Station(String id, int nAvailable) {
        _id = id;
        _nAvailable = nAvailable;
    }

    public String getId() { return _id; }
    
    public int getNPickUps() { return _nPickUps; }

    public int getNAvailable() { return _nAvailable; }

    public int getNDeliveries() { return _nDeliveries; }

    public void setNAvailable(int nAvailable) { this._nAvailable = nAvailable; }

    public void setNDeliveries(int nDeliveries) { this._nDeliveries = nDeliveries; }

    public void setNPickUps(int nPickUps) { this._nPickUps = nPickUps; }

}
