package it.unimib.sd2024;

public class Common {
    public static boolean isCardNumberValid(String cardNumber) {
        return cardNumber.matches("\\d{16}");
    }

    public static boolean isCvvValid(String cvv) {
        return cvv.matches("\\d{3}");
    }
}
