package pt.tecnico.bicloin.hub.domain.exception;

public class NoDocksAvailableException extends FailedPreconditionException {
    
    public NoDocksAvailableException() {
        super("Station has no available docks.");
    }

}
