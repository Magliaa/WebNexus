package it.unimib.sd2024;

public class CommandException extends RuntimeException {
    public CommandException() {
        super();
    }

    public CommandException(String msg) {
        super(msg);
    }
}