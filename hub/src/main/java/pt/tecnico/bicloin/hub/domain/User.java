package pt.tecnico.bicloin.hub.domain;

public class User {
    private String _id;          // 3-10 chars
    private String _name;        // max 30 chars
    private String _phoneNumber;    // variavel -> verificar se começa com '+'

    // TODO confirmar input
    // TODO permitir que o servidor possa arrancar sem users

    public User(String id, String name, String phoneNumber) {
        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
    }

    public String getId() { return _id; }

    public String getName() { return _name; }

    public String getPhoneNumber() { return _phoneNumber; }
    
    public void setId(String id) { this._id = id; }

    public void setName(String name) { this._name = name; }
    
    public void setPhoneNumber(String phoneNumber) { this._phoneNumber = phoneNumber; }

}
