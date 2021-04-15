package pt.tecnico.bicloin.hub.domain.exception;

public class NotEnoughMoneyException extends FailedPreconditionException {
    
    public NotEnoughMoneyException() {
        super("User doesnt have enought balance.");
    }

}
