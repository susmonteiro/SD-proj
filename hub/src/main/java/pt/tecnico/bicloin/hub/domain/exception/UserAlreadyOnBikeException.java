package pt.tecnico.bicloin.hub.domain.exception;

public class UserAlreadyOnBikeException extends FailedPreconditionException {
    
    public UserAlreadyOnBikeException() {
        super("User already has a bicycle.");
    }

}
