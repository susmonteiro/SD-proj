package pt.tecnico.bicloin.hub.domain.exception;

public class UserNotOnBikeException extends FailedPreconditionException {
    
    public UserNotOnBikeException() {
        super("User doesnt have bicycle.");
    }

}
