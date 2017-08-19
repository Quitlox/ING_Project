package honours.ing.banq.info.bean;

import honours.ing.banq.account.BankAccount;

/**
 * Used to represent the balance of a {@link BankAccount}
 *
 * @author Kevin Witlox
 */
public class BalanceBean {

    private Double bankBalance;
    private Double savingsBalance;

    public BalanceBean(BankAccount account) {
        bankBalance = account.getBalance();
        if (account.getSavingsAccount() != null) {
            savingsBalance = account.getSavingsAccount().getBalance();
        }
    }

    public Double getBalance() {
        return bankBalance;
    }

    public Double getSavingsBalance() {
        return savingsBalance;
    }
}
