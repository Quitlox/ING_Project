package honours.ing.banq.account;

import honours.ing.banq.customer.Customer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account of one or more customers.
 *
 * @author Jeffrey Bakker
 */
@Entity
public class BankAccount {

    @Transient
    private static final double INTEREST_ANNUAL = 0.1d;
    @Transient
    public static final double INTEREST_MONTHLY = Math.pow((double) 1 + INTEREST_ANNUAL, 1 / 12) - 1;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Double balance;

    private Double overdraftLimit;

    private Double dailyLow;

    private Double builtInterest;

    @ManyToOne(targetEntity = Customer.class)
    private Customer primaryHolder;

    @ManyToMany(targetEntity = Customer.class)
    private List<Customer> holders;

    /**
     * @deprecated empty constructor for spring
     */
    public BankAccount() {
    }

    public BankAccount(Customer primaryHolder) {
        this.primaryHolder = primaryHolder;
        balance = 0d;
        overdraftLimit = 0d;
        dailyLow = 0d;
        builtInterest = 0d;
        holders = new ArrayList<>();
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

    public Double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(Double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
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

    public Customer getPrimaryHolder() {
        return primaryHolder;
    }

    public void addHolder(Customer holder) {
        holders.add(holder);
    }

    public List<Customer> getHolders() {
        return holders;
    }

    public void removeHolder(Customer holder) {
        holders.remove(holder);
    }

}
