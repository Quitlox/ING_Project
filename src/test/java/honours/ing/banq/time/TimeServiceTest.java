package honours.ing.banq.time;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.auth.InvalidPINError;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
public class TimeServiceTest extends BoilerplateTest {

    @Autowired
    private TimeRepository timeRepository;

    @Test
    public void simulateTime() throws Exception {
        Time time;

        // Reset
        timeService.reset();
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(0));

        // Shift 1
        timeService.simulateTime(365);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(365));

        // Shift 2
        timeService.simulateTime(10);
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(375));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
        account2.token = authService.getAuthToken(account2.username, account2.password).getAuthToken();
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
    public void resetCardNotExpired() throws Exception {
        Time time;

        // Check state
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(0));

        // Transactions
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 100d,
                                         "Test transfer, please ignore");
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, account1.pin, 50d);
        assertThat(infoService.getTransactionsOverview(account1.token, account1.iBan, 10).size(), equalTo(3));
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(50d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo(150d));

        // Alter state, cards not yet expired
        timeService.simulateTime(1000);
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(1000));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
        account2.token = authService.getAuthToken(account2.username, account2.password).getAuthToken();

        // Transactions
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 100d,
                                         "Test transfer, please ignore");
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, account1.pin, 50d);
        assertThat(infoService.getTransactionsOverview(account1.token, account1.iBan, 10).size(), equalTo(6));
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(100d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo(300d));

        // Reset state
        timeService.reset();
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(0));
        Thread.sleep(1500); // Otherwise the current date will be equal to the date of the first three transactions,
                            // causing them to be deleted
        timeService.simulateTime(1);

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
        account2.token = authService.getAuthToken(account2.username, account2.password).getAuthToken();

        assertThat(infoService.getTransactionsOverview(account1.token, account1.iBan, 10).size(), equalTo(3));
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(50d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo(150d));
    }

    @Test(expected = InvalidPINError.class)
    public void resetCardExpired() throws Exception {
        Time time;

        // Check state
        assertThat(timeRepository.findAll().size(), equalTo(1));
        time = timeRepository.findAll().get(0);

        // Transactions
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 100d,
                                         "Test transfer, please ignore");
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, account1.pin, 50d);
        assertThat(infoService.getTransactionsOverview(account1.token, account1.iBan, 10).size(), equalTo(3));
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(50d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo(150d));

        // Alter state, cards expired
        timeService.simulateTime(10000);
        time = timeRepository.findAll().get(0);
        assertThat(time.getShift(), equalTo(10000));

        // Reset authentication
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
        account2.token = authService.getAuthToken(account2.username, account2.password).getAuthToken();

        // Transaction, throws InvalidParamValueError
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, account1.pin, 25d);
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