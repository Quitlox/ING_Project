package honours.ing.banq.info;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.AccessService;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.account.SavingsAccount;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.info.bean.BalanceBean;
import honours.ing.banq.info.bean.BankAccountAccessBean;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionService;
import honours.ing.banq.util.IBANUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 * @since 8-7-2017.
 */
public class InfoServiceTest extends BoilerplateTest {

    // Repositories
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    public void getBalance() throws Exception {
        BalanceBean balanceBean = infoService.getBalance(account1.token, account1.iBan);
        assertThat(balanceBean.getBalance(), equalTo(0d));

        bankAccountService.setOverdraftLimit(account2.token, account2.iBan, 200d);
        transactionService.transferMoney(account2.token, account2.iBan, account1.iBan, account1.username, 200d,
                                         "Test Transaction, please ignore");
        balanceBean = infoService.getBalance(account1.token, account1.iBan);
        assertThat(balanceBean.getBalance(), equalTo(200d));

        bankAccountService.openSavingsAccount(account1.token, account1.iBan);
        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", null, 200d,
                                         "Test Transaction, please ignore");
        balanceBean = infoService.getBalance(account1.token, account1.iBan + "S");
        assertThat(balanceBean.getBalance(), equalTo(0d));
        assertThat(balanceBean.getSavingsBalance(), equalTo(200d));
    }

    @Test(expected = NotAuthorizedError.class)
    public void getBalanceWrongToken() throws Exception {
        infoService.getBalance(account1.token, account2.iBan);
    }

    @Test(expected = NotAuthorizedError.class)
    public void getBalanceInvalidToken() throws Exception {
        infoService.getBalance("-1", account1.iBan);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getBalanceInvalidIBAN() throws Exception {
        infoService.getBalance(account1.token, "-1");
    }

    @Test(expected = InvalidParamValueError.class)
    public void getBalanceWrongIBAN() throws Exception {
        infoService.getBalance(account1.token, IBANUtil.generateIBAN(1234));
    }

    @Test
    public void getTransactionsOverview() throws Exception {
        // TODO: Test sometimes fails because of sorting of returned Lists
        List<Transaction> transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(0));

        // Checked:
        // - nrOfTransactions correct
        // - correct number of transactions returned for different accounts

        // Test Transaction 1 (Deposit)
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 500d);
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(1));
        assertThat(transactions.contains(new Transaction(null, account1.iBan, "Jan Jansen", new Date(), 500d, "Deposit")), is(true));

        // Test Transaction 2
        transactionService.transferMoney(account1.token, account1.iBan, account2
                .iBan, "null", 200d, "Test Transaction 2");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(2));
        assertThat(transactions.contains(new Transaction(account1.iBan, account2.iBan, "null", new Date(), 200d, "Test Transaction 2")), is(true));

        // Test Transaction 3
        transactionService.transferMoney(account1.token, account1.iBan, account2
                .iBan, "null", 150d, "Test Transaction 3");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(2));
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 10);
        assertThat(transactions.size(), equalTo(3));
        assertThat(transactions.contains(new Transaction(account1.iBan, account2.iBan, "null", new Date(), 150d, "Test Transaction 3")), is(true));

        // Test Transaction 4
        transactionService.transferMoney(account2.token, account2.iBan, account1
                .iBan, "null", 100d, "Test Transaction 4");
        transactions = infoService.getTransactionsOverview(account2.token, account2.iBan, 10);
        assertThat(transactions.size(), equalTo(3));
        assertThat(transactions.contains(new Transaction(account2.iBan, account1.iBan, "null", new Date(), 100d, "Test Transaction 4")), is(true));

        // Test Transaction 5 (SavingsAccount)
        bankAccountService.openSavingsAccount(account1.token, account1.iBan);
        transactionService
                .transferMoney(account1.token, account1.iBan, account1.iBan + "S", "null", 150d, "Test Transaction 5");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 10);
        assertThat(transactions.size(), equalTo(5));
        assertThat(transactions.contains(new Transaction(account1.iBan, account1.iBan + "S", "null", new Date(), 150d, "Test Transaction 5")), is(true));

        // Test Transaction 6(SavingsAccount)
        transactionService
                .transferMoney(account1.token, account1.iBan + "S", account1.iBan, "null", 150d, "Test Transaction 6");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 10);
        assertThat(transactions.size(), equalTo(6));
        assertThat(transactions.contains(new Transaction(account1.iBan + "S", account1.iBan, "null", new Date(), 150d, "Test Transaction 6")), is(true));
    }

    @Test
    public void getTransactionsOverviewInterest() throws Exception {
        // Setup accounts
        bankAccountService.openSavingsAccount(account1.token, account1.iBan);
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", null, 1000d, "Test transaction, please ignore");

        // Simulate
        timeService.simulateTime(365);
        account1.token = authService.getAuthToken(account1.username, account1.password).getAuthToken();

        // Test
        List<Transaction> transactions = infoService.getTransactionsOverview(account1.token, account1.iBan + "S", 10);
        assertThat(transactions.size(), equalTo(3));
        assertThat(transactions.contains(new Transaction(null, account1.iBan, "Jan Jansen", new Date(), 1000d, "Deposit")), is(true));
        assertThat(transactions.contains(new Transaction(account1.iBan, account1.iBan + "S", null, new Date(), 1000d, "Test transaction, please ignore")), is(true));

        boolean success = false;
        for (Transaction transaction : transactions) {
            if (transaction.getDescription().equals("Interest")) {
                success = true;
            }
        }
        assertThat(success, is(true));
    }

    @Test(expected = InvalidParamValueError.class)
    public void getTransactionsOverviewNegativeNrOfTransactions() throws Exception {
        infoService.getTransactionsOverview(account1.token, account1.iBan, -1);
    }

    @Test(expected = NotAuthorizedError.class)
    public void getTransactionsOverviewWrongToken() throws Exception {
        infoService.getTransactionsOverview(account1.token, account2.iBan, 2);
    }

    @Test(expected = NotAuthorizedError.class)
    public void getTransactionsOverviewInvalidToken() throws Exception {
        infoService.getTransactionsOverview("-1", account1.iBan, 2);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getTransactionsOverviewWrongIBAN() throws Exception {
        infoService.getTransactionsOverview(account1.token, IBANUtil.generateIBAN(1234), 2);
    }

    @Test(expected = InvalidParamValueError.class)
    public void getTransactionsOverviewInvalidIBAN() throws Exception {
        infoService.getTransactionsOverview(account1.token, "-1", 2);
    }

    @Test
    public void getUserAccess() throws Exception {
        List<UserAccessBean> userAccessBeans;

        userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.size(), equalTo(1));
        assertThat(userAccessBeans.get(0).getiBan(), equalTo(account1.iBan));
        assertThat(userAccessBeans.get(0).getOwner(), equalTo("Jan Jansen"));

        // Provide Access
        accessService.provideAccess(account2.token, account2.iBan, account1.username);
        String secondiBan = bankAccountService.openAdditionalAccount(account1.token).getiBAN();
        bankAccountService.openSavingsAccount(account1.token, account1.iBan);

        Customer customer1 = authService.getAuthorizedCustomer(account1.token);
        BankAccount bankAccount1 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));
        BankAccount bankAccount1Additional = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(secondiBan));
        SavingsAccount bankAccount1Savings = bankAccount1.getSavingsAccount();
        Customer customer2 = authService.getAuthorizedCustomer(account2.token);
        BankAccount bankAccount2 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account2.iBan));

        userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.size(), equalTo(4));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount1, customer1)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount1Additional, customer1)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount1Savings, customer1)), is(true));
        assertThat(userAccessBeans.contains(new UserAccessBean(bankAccount2, customer2)), is(true));

        // Revoke Access
        accessService.revokeAccess(account2.token, account2.iBan, account1.username);
        bankAccountService.closeSavingsAccount(account1.token, account1.iBan + "S");
        bankAccountService.closeAccount(account1.token, secondiBan);

        userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.size(), equalTo(1));
        assertThat(userAccessBeans.get(0).getiBan(), equalTo(account1.iBan));
        assertThat(userAccessBeans.get(0).getOwner(), equalTo("Jan Jansen"));
    }

    @Test(expected = NotAuthorizedError.class)
    public void getUserAccessInvalidToken() throws Exception {
        infoService.getUserAccess("-1");
    }

    @Test
    public void getBankAccountAccess() throws Exception {
        List<BankAccountAccessBean> accessBeans;

        // Only owner
        accessBeans = infoService.getBankAccountAccess(account1.token, account1.iBan);
        assertThat(accessBeans.size(), equalTo(1));
        assertThat(accessBeans.get(0).getUsername(), equalTo(account1.username));

        // One extra holder (provideAccess)
        accessService.provideAccess(account1.token, account1.iBan, account2.username);

        accessBeans = infoService.getBankAccountAccess(account1.token, account1.iBan);
        Customer customer1 = authService.getAuthorizedCustomer(account1.token);
        Customer customer2 = authService.getAuthorizedCustomer(account2.token);
        assertThat(accessBeans.size(), equalTo(2));
        assertThat(accessBeans.contains(new BankAccountAccessBean(customer1)), is(true));
        assertThat(accessBeans.contains(new BankAccountAccessBean(customer2)), is(true));

        // Check other account
        accessBeans = infoService.getBankAccountAccess(account2.token, account2.iBan);
        assertThat(accessBeans.size(), equalTo(1));
        assertThat(accessBeans.get(0).getUsername(), equalTo(account2.username));

        // Only owner (revokeAccess)
        accessService.revokeAccess(account1.token, account1.iBan, account2.username);
        accessBeans = infoService.getBankAccountAccess(account1.token, account1.iBan);
        assertThat(accessBeans.size(), equalTo(1));
        assertThat(accessBeans.get(0).getUsername(), equalTo(account1.username));
    }

    @Test(expected = NotAuthorizedError.class)
    public void getBankAccountAccessWrongToken() throws Exception {
        infoService.getBankAccountAccess(account1.token, account2.iBan);
    }

    @Test(expected = NotAuthorizedError.class)
    public void getBankAccountAccessInvalidToken() throws Exception {
        infoService.getBankAccountAccess("-1", account2.iBan);
    }

}