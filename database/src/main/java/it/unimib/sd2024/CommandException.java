package it.unimib.sd2024;

/**
 * Exception thrown when a command fails.
 */
public class CommandException extends RuntimeException {
    public CommandException() {
        super();
    }

    public CommandException(String msg) {
        super(msg);
    }
}