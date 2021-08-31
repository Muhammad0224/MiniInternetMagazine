package uz.pdp.online.enums;


public enum Currency {
    SUM("so'm"),
    USD("dollar");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static double converter() {
        return 10500;
    }

    public static void showCurrency() {
        System.out.println("\n" + SUM + " -> 1");
        System.out.println(USD + " -> 2");
    }

    public static Currency chooseCardType(int i) {
        switch (i) {
            case 1:
                return SUM;
            case 2:
                return USD;
        }
        return null;
    }
}
