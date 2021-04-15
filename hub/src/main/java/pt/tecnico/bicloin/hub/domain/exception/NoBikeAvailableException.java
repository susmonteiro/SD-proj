package pt.tecnico.bicloin.hub.domain.exception;

public class NoBikeAvailableException extends FailedPreconditionException {
    
    public NoBikeAvailableException() {
        super("Station has no available bike.");
    }

}
