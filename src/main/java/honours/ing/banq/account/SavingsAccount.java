package honours.ing.banq.account;

import javax.persistence.*;

/**
 * @author Kevin Witlox
 * @since 11-8-2017.
 */
@Entity
public class SavingsAccount extends Account{

//    @MapsId
//    @OneToOne(mappedBy = "savingsAccount")
//    @JoinColumn(name = "id")
//    private BankAccount bankAccount;

    public SavingsAccount() {
        this.balance = 0d;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

}
