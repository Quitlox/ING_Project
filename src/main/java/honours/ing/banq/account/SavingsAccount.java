package honours.ing.banq.account;

import javax.persistence.*;

/**
 * @author Kevin Witlox
 * @since 11-8-2017.
 */
@Entity
public class SavingsAccount extends Account{

    @Id @Column(name = "id")
    private Integer id;

    @MapsId
    @OneToOne(mappedBy = "savings_account")
    @JoinColumn(name = "id")
    private BankAccount bankAccount;

    private SavingsAccount() {}

    public SavingsAccount(BankAccount bankAccount) {
        this.balance = 0d;
        this.bankAccount = bankAccount;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

}
