package pt.tecnico.bicloin.hub.domain.exception;

public class EmptyInputException extends InvalidArgumentException {
    
    public EmptyInputException() {
        super("Input cannot be empty.");
    }

}
