package pt.tecnico.rec.domain;

public class User {
    private String _id;
    private int _balance = 0;
    private boolean _onBicycle = false;

    public User(String id, int balance) {
        _id = id;
        _balance = balance;
    }

    public int getBalance() { return _balance; }
    
    public String getId() { return _id; }

    public boolean getOnBicycle() { return _onBicycle; }
    
    public void setBalance(int balance) { this._balance = balance; }

    public void setOnBicyle(boolean onBicycle) { this._onBicycle = onBicycle; }

}
