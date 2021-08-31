package uz.pdp.online.exceptions;

public class CardTypeNotMatch extends RuntimeException{
    public CardTypeNotMatch() {
        super("Card type not match");
    }
}
