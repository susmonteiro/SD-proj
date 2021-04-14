package pt.tecnico.bicloin.hub.domain.exception;

public class InvalidTopUpAmountException extends InvalidArgumentException {
    
    public InvalidTopUpAmountException() {
        super("Invalid amout, please top up with 1-20.");
    }

}
