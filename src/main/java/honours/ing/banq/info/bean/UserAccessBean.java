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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccessBean)) return false;

        UserAccessBean that = (UserAccessBean) o;

        return iBan.equals(that.iBan);
    }

    @Override
    public int hashCode() {
        int result = iBan.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }
}
