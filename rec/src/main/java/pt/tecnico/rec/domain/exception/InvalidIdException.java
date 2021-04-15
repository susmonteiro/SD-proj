package pt.tecnico.rec.domain.exception;

public class InvalidIdException extends InvalidArgumentException {
    public InvalidIdException() {
        super("Invalid id.");
    }
}
