package uz.pdp.online.model;

import lombok.Data;
import uz.pdp.online.enums.CardType;

@Data
public class Card {
    private String cardId;
    private CardType cardType;
    private Double balance;
    private int cardOrdinal;
    private boolean isMain;

    public Card(String cardId, CardType cardType, Double balance, int cardOrdinal) {
        this.cardId = cardId;
        this.cardType = cardType;
        this.balance = balance;
        this.cardOrdinal = cardOrdinal;
    }


}
