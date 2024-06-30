package it.unimib.sd2024;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Common {
    public static boolean isCardNumberValid(String cardNumber) {
        return cardNumber.matches("\\d{16}");
    }

    public static boolean isCvvValid(String cvv) {
        return cvv.matches("\\d{3}");
    }

    public static boolean isCardExpireDateValid(String cardExpireDate) {
        String regex = "^(0[1-9]|1[0-2])/([0-9]{2})$";
        if (!cardExpireDate.matches(regex)) {
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        try {
            YearMonth cardExpiry = YearMonth.parse(cardExpireDate, formatter);
            YearMonth currentYearMonth = YearMonth.now();
            return cardExpiry.isAfter(currentYearMonth) || cardExpiry.equals(currentYearMonth);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
