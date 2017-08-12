package honours.ing.banq.time;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthRepository;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.CustomerRepository;
import honours.ing.banq.transaction.TransactionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
    public void simulateTimeBankAccountInterest() throws Exception {
        Time time;

        // Init
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(utcEqualCalendar(time.getUtc(), Calendar.getInstance()), is(true));

        // Setup Account to receive interest
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");

        // Shift 1
        timeService.simulateTime(SHIFT);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        Calendar shift1 = Calendar.getInstance();
        shift1.add(Calendar.DAY_OF_MONTH, SHIFT);
        assertThat(utcEqualCalendar(time.getUtc(), shift1), is(true));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check BankAccount Interest
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
        Calendar shift2 = Calendar.getInstance();
        shift2.add(Calendar.DAY_OF_MONTH, SHIFT * 2);
        assertThat(utcEqualCalendar(time.getUtc(), shift2), is(true));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check BankAcocunt Interest
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

    @Test
    public void simulateTimeSavingsAccountInterest() throws Exception {
        Time time;

        // Init
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(utcEqualCalendar(time.getUtc(), Calendar.getInstance()), is(true));

        // Setup Account to receive interest
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", account2.username, 1000d,
                                         "Test Transaction, please ignore");

        // Shift 1
        timeService.simulateTime(SHIFT);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        Calendar shift1 = Calendar.getInstance();
        shift1.add(Calendar.DAY_OF_MONTH, SHIFT);
        assertThat(utcEqualCalendar(time.getUtc(), shift1), is(true));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check SavingsAccount Interest
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        double total = STARTING_AMOUNT;
        double charged = STARTING_AMOUNT;
        for (int i = 0; i < SHIFT; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                charged = total;
            }

            double interest = charged > 75000d ? 0.2d : 0.15d;
            total += -charged * (Math.pow(1d + interest, 1d / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)) - 1);
        }

        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), closeTo(charged, PRECISION));

        // Shift 2
        timeService.simulateTime(SHIFT);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        Calendar shift2 = Calendar.getInstance();
        shift2.add(Calendar.DAY_OF_MONTH, SHIFT * 2);
        assertThat(utcEqualCalendar(time.getUtc(), shift2), is(true));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Check SavingsAccount Interest
        for (int i = 0; i < SHIFT; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                charged = total;
            }

            double interest = charged > 75000d ? 0.2d : 0.15d;
            total += -charged * (Math.pow(1d + interest, 1d / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)) - 1);
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
        assertThat(utcEqualCalendar(timeRepository.findAll().get(0).getUtc(), Calendar.getInstance()), is(true));
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

    private boolean utcEqualCalendar(long utc, Calendar calendar) {
        Calendar serverCalendar = Calendar.getInstance();
        serverCalendar.setTime(new Date(utc));

        if (serverCalendar.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) { return false; }
        if (serverCalendar.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) { return false; }
        if (serverCalendar.get(Calendar.DAY_OF_MONTH) != calendar.get(Calendar.DAY_OF_MONTH)) { return false; }
        return true;
    }

}