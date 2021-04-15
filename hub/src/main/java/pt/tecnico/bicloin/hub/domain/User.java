package pt.tecnico.bicloin.hub.domain;

import pt.tecnico.bicloin.hub.domain.exception.InvalidArgumentException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidFileInputException;

public class User {
    public static final short ID_IDX = 0;
    public static final short NAME_IDX = 1;
    public static final short PHONENUMBER_IDX = 2;

    private String _id;          // 3-10 chars
    private String _name;        // max 30 chars
    private String _phoneNumber;    // variavel -> verificar se começa com '+'

    public User(String id, String name, String phoneNumber) throws InvalidFileInputException {
        try {
            checkId(id);
            checkName(name);
            checkPhoneNumber(phoneNumber);
        } catch (InvalidArgumentException e) {
            throw new InvalidFileInputException(e.getMessage());
        }

        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
    }

    public User(String[] fields) throws InvalidFileInputException {
        this(fields[ID_IDX], fields[NAME_IDX], fields[PHONENUMBER_IDX]);
    }

    public static void checkId(String id) throws InvalidArgumentException {
        if (id.length() < 3 || id.length() > 10) throw new InvalidArgumentException("ID " + id + " is invalid.\nID has to be between 3 and 10 characters.");
    }
    public static void checkName(String name) throws InvalidArgumentException {
        if (name.length() > 30) throw new InvalidArgumentException("Name " + name + " is invalid\n.Name cannot have more than 30 characters.");
    }
    public static void checkPhoneNumber(String phoneNumber) throws InvalidArgumentException {
        if (!phoneNumber.startsWith("+")) throw new InvalidArgumentException("Phone number " + phoneNumber + " is invalid.\nPhone number has to contain a country code}.");
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
