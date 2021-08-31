package uz.pdp.online.enums;

public enum CardType {
    UZCARD(Currency.SUM),
    HUMO(Currency.SUM),
    VISA(Currency.USD),
    MASTERCARD(Currency.USD);

    private Currency currency;

    CardType(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public static void showCardTypes(){
        System.out.println("\n" + UZCARD + " -> 1");
        System.out.println(HUMO + " -> 2");
        System.out.println(VISA + " -> 3");
        System.out.println(MASTERCARD + " -> 4");
    }

    public static CardType chooseCardType(int i){
        switch (i){
            case 1:
                return UZCARD;
            case 2:
                return HUMO;
            case 3:
                return VISA;
            case 4:
                return MASTERCARD;
        }
        return null;
    }
}
