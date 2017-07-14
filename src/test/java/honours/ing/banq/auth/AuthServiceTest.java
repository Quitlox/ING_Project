package honours.ing.banq.auth;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.util.IBANUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jeffrey
 * @since 9-6-17
 */
public class AuthServiceTest extends BoilerplateTest {

    @Test
    public void getAuthToken() throws Exception {
        String token = authService.getAuthToken(account1.username, account1.password).getAuthToken();
        account1.token = token;

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test(expected = AuthenticationError.class)
    public void getAuthTokenInvalidUsername() throws Exception {
        authService.getAuthToken("", account1.password);
    }

    @Test(expected = AuthenticationError.class)
    public void getAuthTokenInvalidPassword() throws Exception {
        authService.getAuthToken(account1.username, "");
    }

    @Test(expected = AuthenticationError.class)
    public void getAuthTokenInvalidUsernameAndPassword() throws Exception {
        authService.getAuthToken("", "");
    }

    @Test
    public void getAuthorizedCustomer() throws Exception {
        String token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        assertNotNull(token);
        assertTrue(token.length() > 0);

        Customer customer = authService.getAuthorizedCustomer(token);
        assertEquals(account1.username, customer.getUsername());
        assertEquals(account1.password, customer.getPassword());
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedCustomerNullToken() throws Exception {
        authService.getAuthorizedCustomer(null);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedCustomerEmptyToken() throws Exception {
        authService.getAuthorizedCustomer("");
    }

    @Test(expected = NotAuthorizedError.class)
    public void getAuthorizedCustomerInvalidToken() throws Exception {
        authService.getAuthorizedCustomer("invalidToken");
    }

    @Test(expected = NotAuthorizedError.class)
    public void getAuthorizedCustomerExpiredToken() throws Exception {
        timeService.simulateTime(2);
        authService.getAuthorizedCustomer(account1.token);
    }

    @Test
    public void getAuthorizedAccount() throws Exception {
        BankAccount account = authService.getAuthorizedAccount(account1.iBan, account1.cardNumber,
                                                               account1.pin);

        assertNotNull(account);
        assertEquals(account1.iBan, IBANUtil.generateIBAN(account));
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountNullIBAN() throws Exception {
        authService.getAuthorizedAccount(null, account1.cardNumber, account1.pin);
        fail();
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountEmptyIBAN() throws Exception {
        authService.getAuthorizedAccount("", account1.cardNumber, account1.pin);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountWrongIBAN() throws Exception {
        authService.getAuthorizedAccount(account2.iBan, account1.cardNumber, account1.pin);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountNullCardNumber() throws Exception {
        authService.getAuthorizedAccount(account1.iBan, null, account1.pin);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountEmptyCardNumber() throws Exception {
        authService.getAuthorizedAccount(account1.iBan, "", account1.pin);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountInvalidCardNumber() throws Exception {
        authService.getAuthorizedAccount(account1.iBan, "-1", account1.pin);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getAuthorizedAccountNullPin() throws Exception {
        authService.getAuthorizedAccount(account1.iBan, account1.cardNumber, null);
    }

    @Test(expected = InvalidPINError.class)
    public void getAuthorizedAccountEmptyPin() throws Exception {
        authService.getAuthorizedAccount(account1.iBan, account1.cardNumber, "");
    }

    @Test(expected = InvalidPINError.class)
    public void getAuthorizedAccountExpiredCard() throws Exception {
        timeService.simulateTime(10000);
        authService.getAuthorizedAccount(account1.iBan, account1.cardNumber, account1.pin);
    }

}