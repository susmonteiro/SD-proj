package pt.tecnico.rec.domain.exception;

public class NoRegisterValueSetException extends InvalidArgumentException {
    public NoRegisterValueSetException() {
        super("No register value set.");
    }
}
