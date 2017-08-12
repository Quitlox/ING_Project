package honours.ing.banq.account;

import javax.persistence.Entity;

/**
 * @author Kevin Witlox
 * @since 12-8-2017.
 */
@Entity
public class Account {

    protected Double balance;

    protected Double dailyLow;

    protected Double builtInterest;

    public Account() {
    }

    public Double getBalance() {
        return balance;
    }

    public void subBalance(Double balance) {
        this.balance -= balance;
        this.dailyLow = this.balance;
    }

    public void resetBuiltInterest() {
        this.builtInterest = 0d;
    }

    public void addBuiltInterest(Double builtInterest) {
        this.builtInterest += builtInterest;
    }

    public Double getBuiltInterest() {
        return builtInterest;
    }

    public Double getDailyLow() {
        return dailyLow;
    }

    public void setDailyLow(Double dailyLow) {
        this.dailyLow = dailyLow;
    }

}
