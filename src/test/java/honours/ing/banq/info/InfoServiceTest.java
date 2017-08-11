package honours.ing.banq.info;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.AccessService;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.info.bean.BalanceBean;
import honours.ing.banq.info.bean.BankAccountAccessBean;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionService;
import honours.ing.banq.util.IBANUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 * @since 8-7-2017.
 */
public class InfoServiceTest extends BoilerplateTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccessService accessService;

    @Test
    public void getBalance() throws Exception {
        BalanceBean balanceBean = infoService.getBalance(account1.token, account1.iBan);
        assertThat(balanceBean.getBalance(), equalTo(0d));
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

        Transaction transaction;

        // Checked:
        // - nrOfTransactions correct
        // - correct number of transactions returned for different accounts

        // Test Transaction 1 (Deposit)
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 500d);
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(1));
        transaction = transactions.get(0);
        assertThat(transaction.getAmount(), equalTo(500d));
        assertThat(transaction.getSource(), nullValue());
        assertThat(transaction.getDestination(), equalTo(account1.iBan));

        // Test Transaction 2
        transactionService.transferMoney(account1.token, account1.iBan, account2
                .iBan, "null", 200d, "Test Transaction 2");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(2));
        transaction = transactions.get(1);
        assertThat(transaction.getAmount(), equalTo(200d));
        assertThat(transaction.getSource(), equalTo(account1.iBan));
        assertThat(transaction.getDestination(), equalTo(account2.iBan));

        // Test Transaction 3
        transactionService.transferMoney(account1.token, account1.iBan, account2
                .iBan, "null", 150d, "Test Transaction 3");
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 2);
        assertThat(transactions.size(), equalTo(2));
        transactions = infoService.getTransactionsOverview(account1.token, account1.iBan, 10);
        assertThat(transactions.size(), equalTo(3));
        transaction = transactions.get(2);
        assertThat(transaction.getAmount(), equalTo(150d));
        assertThat(transaction.getSource(), equalTo(account1.iBan));
        assertThat(transaction.getDestination(), equalTo(account2.iBan));

        // Test Transaction 4
        transactionService.transferMoney(account2.token, account2.iBan, account1
                .iBan, "null", 100d, "Test Transaction 4");
        transactions = infoService.getTransactionsOverview(account2.token, account2.iBan, 10);
        assertThat(transactions.size(), equalTo(3));
        transaction = transactions.get(2);
        assertThat(transaction.getAmount(), equalTo(100d));
        assertThat(transaction.getSource(), equalTo(account2.iBan));
        assertThat(transaction.getDestination(), equalTo(account1.iBan));
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
        userAccessBeans = infoService.getUserAccess(account1.token);
        assertThat(userAccessBeans.size(), equalTo(2));

        boolean suc1 = false, suc2 = false;
        for (UserAccessBean userAccessBean : userAccessBeans) {
            if (userAccessBean.getiBan().equals(account1.iBan) && userAccessBean.getOwner().equals("Jan Jansen")) {
                suc1 = true;
            }

            if (userAccessBean.getiBan().equals(account2.iBan) && userAccessBean.getOwner().equals("Piet Pietersen")) {
                suc2 = true;
            }
        }

        assertThat(suc1 && suc2, is(true));

        // Revoke Access
        accessService.revokeAccess(account2.token, account2.iBan, account1.username);
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
        assertThat(accessBeans.size(), equalTo(2));
        boolean suc1 = false, suc2 = false;
        for (BankAccountAccessBean accessBean : accessBeans) {
            if (accessBean.getUsername().equals(account1.username)) {
                suc1 = true;
            }

            if (accessBean.getUsername().equals(account2.username)) {
                suc2 = true;
            }
        }
        assertThat(suc1 & suc2, is(true));

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