package honours.ing.banq.util;

import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;

public class IBANUtil {

    private static final String COUNTRY_CODE = "NL";
    private static final String BANK_CODE = "BANQ";

    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    public static boolean isValidIBAN(String iBAN) {
        if (iBAN.length() <= 10) {
            return false;
        }

        String check = iBAN.substring(2, 4);

        return calculateChecksum(iBAN.substring(4, iBAN.length())).equals(check);
    }

    public static long getAccountNumber(String iBAN) throws InvalidParamValueError {
        if (!IBANUtil.isValidIBAN(iBAN)) {
            throw new InvalidParamValueError("The given IBAN is invalid");
        }

        return Long.parseLong(iBAN.substring(8, iBAN.length()));
    }

    public static String generateIBAN(BankAccount account) {
        return generateIBAN(account.getId());
    }

    public static String generateIBAN(long accountNumber) {
        StringBuilder res = new StringBuilder(String.valueOf(accountNumber));
        while (res.length() < ACCOUNT_NUMBER_LENGTH) {
            res.insert(0, '0');
        }

        // Insert bank code
        res.insert(0, BANK_CODE);

        // Insert checksum
        res.insert(0, calculateChecksum(res.toString()));

        // Insert country code
        res.insert(0, COUNTRY_CODE);

        return res.toString();
    }

    public static String calculateChecksum(String accountNumber) {
        StringBuilder res = new StringBuilder();
        for (int i = accountNumber.length() - 1; i >= 0; i--) {
            char c = accountNumber.charAt(i);
            if (Character.isDigit(c)) {
                res.insert(0, c);
            } else {
                res.insert(0, ((int) c) - 55);
            }
        }

        long nr = Long.parseLong(res.toString());
        int r = (int) (nr % 97);

        return r < 10 ? "0" + r : String.valueOf(r);
    }

}
