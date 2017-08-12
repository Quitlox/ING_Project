package honours.ing.banq.account;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.bean.NewAccountBean;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.util.IBANUtil;
import honours.ing.banq.util.StringUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.OperationNotSupportedException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 * @since 5-8-17
 */
public class BankAccountServiceTest extends BoilerplateTest {

    // Repositories
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    public void openAccount() throws Exception {
        String username = StringUtil.generate(10);
        String password = StringUtil.generate(10);

        NewAccountBean account = bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), username, password);

        // Get the auth token
        String token = authService.getAuthToken(username, password).getAuthToken();
        assertThat(token, notNullValue());

        // Check user access
        List<UserAccessBean> list = infoService.getUserAccess(token);
        assertThat(list.size(), equalTo(1));
        assertThat(list.get(0).getOwner(), equalTo("University of Twente"));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueSSN() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", account1.ssn,
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), StringUtil.generate(10), StringUtil.generate(10));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueEmail() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                account1.email, StringUtil.generate(10), StringUtil.generate(10));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueUsername() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), account1.username, StringUtil.generate(10));
    }

    @Test
    public void openAdditionalAccount() throws Exception {
        NewAccountBean account = bankAccountService.openAdditionalAccount(account1.token);

        List<UserAccessBean> list = infoService.getUserAccess(account1.token);
        assertThat(list.size(), equalTo(2));
    }

    @Test
    public void closeAccount() throws Exception {
        bankAccountService.closeAccount(account1.token, account1.iBan);
        bankAccountService.closeAccount(account2.token, account2.iBan);
    }

    @Test(expected = InvalidParamValueError.class)
    public void closeAccountPositiveBalance() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 1000d);
        bankAccountService.closeAccount(account1.token, account1.iBan);
    }

    @Test(expected = InvalidParamValueError.class)
    public void closeAccountNegativeBalance() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 500d,
                                         "Test Transaction, please ignore");
        bankAccountService.closeAccount(account1.token, account1.iBan);
    }

    public void closeAccountSavingsAccountNonZeroBalance() throws Exception {
        bankAccountService.openSavingsAccount(account1.token, account1.iBan);

        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", null, 500d,
                                         "Test Transaction, please ignore");
        exception.expect(InvalidParamValueError.class);
        bankAccountService.closeAccount(account1.token, account1.iBan);
    }

    @Test(expected = NotAuthorizedError.class)
    public void closeAccountNonExistant() throws Exception {
        bankAccountService.closeAccount(account1.token, account1.iBan);
        bankAccountService.closeAccount(account1.token, account1.iBan);
    }

    @Test(expected = InvalidParamValueError.class)
    public void closeNonExistingAccount() throws Exception {
        bankAccountService.closeAccount(account1.token, IBANUtil.generateIBAN(123456789));
    }

    @Test
    public void closeAccountNonPrimaryHolder() throws Exception {
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        bankAccountService.closeAccount(account2.token, account1.iBan);
    }

    @Test
    public void setOverdraftLimit() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);

        // Should be able to transfer up to 1000
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");

        // Close Account
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 1000d);
        bankAccountService.closeAccount(account1.token, account1.iBan);
    }

    @Test(expected = InvalidParamValueError.class)
    public void setOverdraftLimitOverdraftWithDefaultOverdraft() throws Exception {
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");
    }

    @Test
    public void setOverdraftLimitOverdraftWithAlteredOverdraft() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");
        exception.expect(InvalidParamValueError.class);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, account2.username, 1d,
                                         "Test Transaction, please ignore");
    }

    @Test
    public void setOverdraftLimitNonPrimaryHolder() throws Exception {
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        bankAccountService.setOverdraftLimit(account2.token, account1.iBan, 1000d);

        // Should be able to transfer up to 1000
        transactionService.transferMoney(account2.token, account1.iBan, account2.iBan, account2.username, 1000d,
                                         "Test Transaction, please ignore");

        // Close Account
        transactionService.depositIntoAccount(account2.iBan, account1.cardNumber, account1.pin, 1000d);
        bankAccountService.closeAccount(account2.token, account1.iBan);
    }

    @Test
    public void getOverdraftLimit() throws Exception {
        assertThat(bankAccountService.getOverdraftLimit(account1.token, account1.iBan).getOverdraftLimit(),
                   equalTo(0d));
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 500d);
        assertThat(bankAccountService.getOverdraftLimit(account1.token, account1.iBan).getOverdraftLimit(),
                   equalTo(500d));
    }

    @Test
    public void openSavingsAccount() throws Exception {
        bankAccountService.openSavingsAccount(account1.token, account1.iBan);
        Customer customer = authService.getAuthorizedCustomer(account1.token);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));

        List<UserAccessBean> userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount, customer)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount.getSavingsAccount(), customer)), is(true));
        assertThat(userAccessBeans.size(), equalTo(2));
    }

    @Test
    public void openSavingsAccountNonPrimaryHolder() throws Exception {
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        bankAccountService.openSavingsAccount(account2.token, account1.iBan);

        Customer customer = authService.getAuthorizedCustomer(account1.token);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account2.iBan));

        List<UserAccessBean> userAccessBeans = infoService.getUserAccess(account2.token);
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount, customer)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount.getSavingsAccount(), customer)), is(false));
        assertThat(userAccessBeans.size(), equalTo(1));
    }

    @Test(expected = NotAuthorizedError.class)
    public void openSavingsAccountWrongIBAN() throws Exception {
        bankAccountService.openSavingsAccount(account1.token, account2.iBan);
    }

    @Test(expected = NotAuthorizedError.class)
    public void openSavingsAccountNonExistantIBAN() throws Exception {
        bankAccountService.openSavingsAccount(account1.token, "-1");
    }

    @Test(expected = InvalidParamValueError.class)
    public void openSavingsAccountOnSavingsAccount() throws Exception {
        bankAccountService.openSavingsAccount(account1.token, account1.iBan + "S");
    }

    @Test
    public void closeSavingsAccount() throws Exception {
        bankAccountService.closeSavingsAccount(account1.token, account1.iBan);
        Customer customer = authService.getAuthorizedCustomer(account1.token);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));

        List<UserAccessBean> userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount, customer)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount.getSavingsAccount(), customer)), is(false));
        assertThat(userAccessBeans.size(), equalTo(1));
    }

    @Test
    public void closeSavingsAccountNonExistantIBAN() throws Exception {
        bankAccountService.closeSavingsAccount(account1.token, " -1");
    }

    @Test
    public void closeSavingsAccountWrongIBAN() throws Exception {
        bankAccountService.closeSavingsAccount(account1.token, account2.iBan);
    }

    @Test
    public void closeSavingsAccountNonPrimaryHolder() throws Exception {
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        bankAccountService.closeSavingsAccount(account2.token, account1.iBan);

        Customer customer = authService.getAuthorizedCustomer(account1.token);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));

        List<UserAccessBean> userAccessBeans = infoService.getUserAccess(account2.token);
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount, customer)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount.getSavingsAccount(), customer)), is(false));
        assertThat(userAccessBeans.size(), equalTo(1));
    }

}