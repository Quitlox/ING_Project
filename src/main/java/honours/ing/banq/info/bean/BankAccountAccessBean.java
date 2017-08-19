package honours.ing.banq.info.bean;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.customer.Customer;

/**
 * Used to represent a user that has access to a certain {@link BankAccount}. Stores the username of
 * said user.
 *
 * @author Kevin Witlox
 */
public class BankAccountAccessBean {

    private String username;

    public BankAccountAccessBean(Customer customer) {
        this.username = customer.getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccountAccessBean)) return false;

        BankAccountAccessBean that = (BankAccountAccessBean) o;

        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
