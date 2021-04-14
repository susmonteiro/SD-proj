package pt.tecnico.bicloin.hub.domain.exception;

public class InvalidUserException extends InvalidArgumentException {
    
    public InvalidUserException() {
        super("Invalid user.");
    }

}
