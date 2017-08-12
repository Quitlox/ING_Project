package honours.ing.banq.account;

import honours.ing.banq.customer.Customer;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account of one or more customers.
 *
 * @author Jeffrey Bakker
 */
@Entity
public class BankAccount extends Account {

    @Transient
    private static final double INTEREST_ANNUAL = 0.1d;
    @Transient
    public static final double INTEREST_MONTHLY = Math.pow(1d + INTEREST_ANNUAL, 1d / 12d) - 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double overdraftLimit;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private SavingsAccount savingsAccount;

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
        holders = new ArrayList<>();

        savingsAccount = null;
    }

    public Integer getId() {
        return id;
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

    public Customer getPrimaryHolder() {
        return primaryHolder;
    }

    public SavingsAccount getSavingsAccount() {
        return savingsAccount;
    }

    public void setSavingsAccount(SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
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
