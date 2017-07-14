package honours.ing.banq.card;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.customer.Customer;

import javax.persistence.*;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Represents a physical card that a holder of an bank account can use to make pin transactions.
 *
 * @author Kevin Witlox
 */
@Entity
public class Card {

    @SuppressWarnings("NumericOverflow")
    public static final int DURABILITY = 5; // 5 years

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(targetEntity = Customer.class)
    private Customer holder;

    @ManyToOne(targetEntity = BankAccount.class)
    private BankAccount account;

    @Column(unique = true, nullable = false)
    private String cardNumber;
    private String pin; // TODO: add hashing
    private Date expirationDate;

    private int attempts;

    private boolean blocked;
    private boolean invalidated;

    /**
     * @deprecated empty constructor for spring
     */
    public Card() {
    }

    public Card(String token, Customer holder, BankAccount account, String cardNumber, Date currDate) {
        this.holder = holder;
        this.account = account;
        this.cardNumber = cardNumber;
        this.invalidated = false;
        this.blocked = false;
        this.attempts = 0;

        pin = generatePin(token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, DURABILITY);
        expirationDate = calendar.getTime();
    }

    public Card(String token, Customer holder, BankAccount account, String cardNumber, String pin, Date currDate) {
        this(token, holder, account, cardNumber, currDate);
        this.pin = pin;
    }

    public Integer getId() {
        return id;
    }

    public Customer getHolder() {
        return holder;
    }

    public BankAccount getAccount() {
        return account;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public int getAttempts() {
        return attempts;
    }

    public void resetAttempts() {
        attempts = 0;
    }

    public void addAttempt() {
        attempts++;

        if (attempts == 3) {
            block();
            attempts = 0;
        }
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void invalidate() {
        this.invalidated = true;
    }

    public void block() {
        this.blocked = true;
    }

    public void unblock() {
        this.blocked = false;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    private static String generatePin(String authToken) {
        StringBuilder res = new StringBuilder();
        Random rnd = new SecureRandom((authToken + System.currentTimeMillis()).getBytes());

        for (int i = 0; i < 4; i++) {
            res.append(rnd.nextInt(10));
        }

        return res.toString();
    }

}
