package honours.ing.banq.time;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthRepository;
import honours.ing.banq.auth.InvalidPINError;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.CustomerRepository;
import honours.ing.banq.transaction.TransactionRepository;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
public class TimeServiceTest extends BoilerplateTest {

    // Repositories
    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Fields
    private static final double PRECISION = 0.01;
    private static final double STARTING_AMOUNT = -1000d;
    private static final int SHIFT = 305;

    @Test
    public void simulateTime() throws Exception {
        Time time;

        // Init
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(0));

        // Setup Account to receive interest
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");

        // Shift 1
        timeService.simulateTime(SHIFT);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(SHIFT));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check Interest
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        double total = STARTING_AMOUNT;
        double charged = STARTING_AMOUNT;
        for (int i = 0; i < SHIFT; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                charged = total;
            }

            total += charged * BankAccount.INTEREST_MONTHLY / calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), closeTo(charged, PRECISION));

        // Shift 2
        timeService.simulateTime(SHIFT);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(SHIFT + SHIFT));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check Interest
        for (int i = 0; i < SHIFT; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                charged = total;
            }

            total += charged * BankAccount.INTEREST_MONTHLY / calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), closeTo(charged, PRECISION));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
    }

    @Test(expected = InvalidParamValueError.class)
    public void simulateTimeZeroNrOfDays() throws Exception {
        timeService.simulateTime(0);
    }

    @Test(expected = InvalidParamValueError.class)
    public void simulateTimeNegativeNrOfDays() throws Exception {
        timeService.simulateTime(-1);
    }

    @Test
    public void reset() throws Exception {
        timeService.reset();

        assertThat(authRepository.findAll(), empty());
        assertThat(bankAccountRepository.findAll(), empty());
        assertThat(cardRepository.findAll(), empty());
        assertThat(customerRepository.findAll(), empty());
        assertThat(transactionRepository.findAll(), empty());

        assertThat(timeRepository.findAll().size(), equalTo(1));
        assertThat(timeRepository.findAll().get(0).getShift(), equalTo(0));
    }

    @Test
    public void getDate() throws Exception {
        assertThat(timeService.getDate().getDate(), equalTo((new SimpleDateFormat("yyyy-MM-dd")).format(new Date())));

        timeService.simulateTime(5);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        assertThat(timeService.getDate().getDate(),
                   equalTo((new SimpleDateFormat("yyyy-MM-dd")).format(calendar.getTime())));

        timeService.reset();
        assertThat(timeService.getDate().getDate(), equalTo((new SimpleDateFormat("yyyy-MM-dd")).format(new Date())));
    }

}