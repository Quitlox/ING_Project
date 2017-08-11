package honours.ing.banq.account.bean;

/**
 * @author Kevin Witlox
 * @since 5-8-2017.
 */
public class OverdraftLimitBean {

    private Double overdraftLimit;

    public OverdraftLimitBean(Double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(Double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
}
