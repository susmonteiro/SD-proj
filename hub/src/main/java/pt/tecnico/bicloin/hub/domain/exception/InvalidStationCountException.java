package pt.tecnico.bicloin.hub.domain.exception;

public class InvalidStationCountException extends InvalidArgumentException {
    
    public InvalidStationCountException() {
		super("Invalid number, please scan 0 or higher.");
	}

}