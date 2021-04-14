package pt.tecnico.bicloin.hub.domain.exception;

public class InvalidPhoneNumberException extends InvalidArgumentException {
    
    public InvalidPhoneNumberException() {
		super("Given phone number dont match user.");
	}
}
