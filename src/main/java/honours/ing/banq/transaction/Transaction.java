package honours.ing.banq.transaction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents a transaction that can be made between two bank accounts.
 * @author Jeffrey Bakker
 * @since 24-4-17
 */
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String source;
    private String destination;
    private String targetName;

    private Date date;
    private Double amount;
    private String description;

    /**
     * An empty constructor for the spring framework.
     * @deprecated
     */
    private Transaction() {}

    public Transaction(String source, String destination, String targetName, Date date, Double amount, String description) {
        this.source = source;
        this.destination = destination;
        this.targetName = targetName;
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getTargetName() {
        return targetName;
    }

    public Date getDate() {
        return date;
    }

    public Double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;

        Transaction that = (Transaction) o;

        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;
        if (targetName != null ? !targetName.equals(that.targetName) : that.targetName != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (targetName != null ? targetName.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
