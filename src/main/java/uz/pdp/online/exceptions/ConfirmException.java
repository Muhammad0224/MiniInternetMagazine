package uz.pdp.online.exceptions;

public class ConfirmException extends RuntimeException{
    public ConfirmException() {
        super("The confirmation is not the same as the password ");
    }
}
