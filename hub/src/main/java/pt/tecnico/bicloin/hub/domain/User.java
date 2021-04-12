package pt.tecnico.bicloin.hub.domain;

import pt.tecnico.bicloin.hub.domain.exception.InvalidFileInputException;

public class User {
    public static final short ID_IDX = 0;
    public static final short NAME_IDX = 1;
    public static final short PHONENUMBER_IDX = 2;

    private String _id;          // 3-10 chars
    private String _name;        // max 30 chars
    private String _phoneNumber;    // variavel -> verificar se começa com '+'

    public User(String id, String name, String phoneNumber) throws InvalidFileInputException {
        if (id.length() < 3 || id.length() > 10) 
            throw new InvalidFileInputException("ID " + id + " is invalid.\nID has to be between 3 and 10 characters.");
        if (name.length() > 30)
            throw new InvalidFileInputException("Name " + name + " is invalid\n.Name cannot have more than 30 characters.");
        if (!phoneNumber.startsWith("+"))
            throw new InvalidFileInputException("Phone number " + phoneNumber + " is invalid.\nPhone number has to contain a country code.");

        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
    }

    public User(String[] fields) throws InvalidFileInputException {
        this(fields[0], fields[1], fields[2]);
    }

    public String getId() { return _id; }

    public String getName() { return _name; }

    public String getPhoneNumber() { return _phoneNumber; }
    
    public void setId(String id) { this._id = id; }

    public void setName(String name) { this._name = name; }
    
    public void setPhoneNumber(String phoneNumber) { this._phoneNumber = phoneNumber; }

    public String toString() {
        return "Name: " + _name + "\n" +
            "Phone Number: " + _phoneNumber;
    }

}
