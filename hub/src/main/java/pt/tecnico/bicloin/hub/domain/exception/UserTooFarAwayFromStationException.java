package pt.tecnico.bicloin.hub.domain.exception;

public class UserTooFarAwayFromStationException extends FailedPreconditionException {
 
    public UserTooFarAwayFromStationException() {
        super("User is too far away from station to request a bicycle.");
    }

}
