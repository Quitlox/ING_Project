package honours.ing.banq.info.bean;

import honours.ing.banq.account.Account;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.util.IBANUtil;

/**
 * Used to represent an account a certain user has access to. Stores its iBAN and its owner (can be
 * different than the user).
 *
 * @author Kevin Witlox
 */
public class UserAccessBean {

    private String iBan;
    private String owner;

    public UserAccessBean(Account account, Customer customer) {
        this.iBan = IBANUtil.generateIBAN(account);
        this.owner = customer.getName();
    }

    public String getiBan() {
        return iBan;
    }

    public void setiBan(String iBan) {
        this.iBan = iBan;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
