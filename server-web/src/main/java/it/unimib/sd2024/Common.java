package it.unimib.sd2024;

public class Common {
    public static boolean isCardNumberValid(String cardNumber) {
        return cardNumber.matches("\\d{16}");
    }

    public static boolean isCvvValid(String cvv) {
        return cvv.matches("\\d{3}");
    }

    public static boolean isCardExpireDateValid(String cardExpireDate) {
        String regex = "^(0[1-9]|1[0-2])\/([0-9]{2})$";
        return cardExpireDate.matches(regex);
    }
}
