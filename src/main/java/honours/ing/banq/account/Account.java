package honours.ing.banq.account;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Kevin Witlox
 * @since 12-8-2017.
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected Double balance;

    protected Double dailyLow;

    protected Double builtInterest;

    protected Double overdraftLimit;

    public Account() {
    }

    public Integer getId() {
        return id;
    }

    public Double getBalance() {
        return balance;
    }

    public void subBalance(Double balance) {
        this.balance -= balance;
        this.dailyLow = this.balance;
    }

    public void addBalance(Double balance) {
        this.balance += balance;
    }

    public Double getDailyLow() {
        return dailyLow;
    }

    public void setDailyLow(Double dailyLow) {
        this.dailyLow = dailyLow;
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

    public Double getOverdraftLimit() {
        return overdraftLimit;
    }


}
