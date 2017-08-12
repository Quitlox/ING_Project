package honours.ing.banq.transaction;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.auth.InvalidPINError;
import honours.ing.banq.auth.NotAuthorizedError;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 */
public class TransactionServiceTest extends BoilerplateTest {

    @Test
    public void depositIntoAccount() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200.0d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
    }

    @Test(expected = InvalidPINError.class)
    public void depositIntoAccountWrongPinCode() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, "9999", 200.0d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void depositIntoAccountNegativeAmount() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, -200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void depositIntoAccountWrongIBAN() throws Exception {
        transactionService.depositIntoAccount(account2.iBan, account1.cardNumber, account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void depositIntoAccountWrongIBANType() throws Exception {
        transactionService.depositIntoAccount("null", account1.cardNumber, account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void depositIntoAccountWrongPinCard() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, "0", account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
    }


    @Test
    public void payFromAccount() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);

        // Transaction
        transactionService.payFromAccount(account1.iBan, account2.iBan,
                                          account1.cardNumber, account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (200d));
    }

    @Test(expected = InvalidPINError.class)
    public void payFromAccountWrongPinCode() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);

        // Transaction
        transactionService.payFromAccount(account1.iBan, account2.iBan,
                                          account1.cardNumber, "-1", 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void payFromAccountWrongPinCard() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);

        // Transaction
        transactionService.payFromAccount(account1.iBan, account2.iBan,
                                          "-1", account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void payFromAccountWrongSourceiBan() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);

        // Transaction
        transactionService.payFromAccount("null", account2.iBan,
                                          account1.cardNumber, account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void payFromAccountWrongTargetiBan() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);

        // Transaction
        transactionService.payFromAccount(account1.iBan, "null",
                                          account1.cardNumber, account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void payFromAccountNotEnoughBalance() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));

        // Transaction
        transactionService.payFromAccount(account1.iBan, account2.iBan,
                                          account1.cardNumber, account1.pin, 201d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void payFromAccountNegativeAmount() throws Exception {
        // Make sure source iBan has balance
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber,
                                              account1.pin, 200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));

        // Transaction
        transactionService.payFromAccount(account1.iBan, account2.iBan,
                                          account1.cardNumber, account1.pin, -200d);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test
    public void transferMoneyBankAccount() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (200d));
    }

    @Test
    public void transferMoneySavingsAccount() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", "Piet Pietersen",
                                         200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
        assertThat(infoService.getBalance(account1.token, account1.iBan + "S").getBalance(), equalTo
                (200d));
        transactionService.transferMoney(account1.token, account1.iBan + "S", account1.iBan, "Piet Pietersen",
                                         200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan + "S").getBalance(), equalTo
                (0d));
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
    }

    @Test
    public void transferMoneySavingsAccountWrongDestination() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);
        transactionService.transferMoney(account1.token, account1.iBan, account1.iBan + "S", "Piet Pietersen",
                                         200d, "Geld");

        transactionService.transferMoney(account1.token, account1.iBan + "S", account2.iBan, "Piet Pietersen",
                                         200d, "Geld");
    }

    @Test
    public void transferMoneySavingsAccountWrongSource() throws Exception {
        transactionService.depositIntoAccount(account2.iBan, account2.cardNumber, account2.pin, 200d);

        transactionService.transferMoney(account2.token, account2.iBan, account1.iBan + "S", "Piet Pietersen",
                                         200d, "Geld");
    }

    @Test(expected = NotAuthorizedError.class)
    public void transferMoneyNotAuthorized() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account2.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = NotAuthorizedError.class)
    public void transferMoneyUnauthorizedSourceIBAN() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account1.token, account2.iBan, account2.iBan, "Piet Pietersen",
                                         200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void transferMoneyNonExistantTargetIBAN() throws Exception

    {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account1.token, account1.iBan, "null", "Piet Pietersen", 200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void transferMoneyNegativeBalance() throws Exception {
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         -200d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test(expected = InvalidParamValueError.class)
    public void transferMoneyNotEnoughBalance() throws Exception {
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 200d);

        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         201d, "Geld");
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (200d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));
    }

    @Test
    public void transferMoneyCustomOverdraftLimit() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         1000d, "Geld");
    }

    @Test
    public void transferMoneyCustomOverdraftLimitNotEnoughBalance() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         1000d, "Geld");
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1500d);
        exception.expect(InvalidParamValueError.class);
        transactionService.transferMoney(account1.token, account1.iBan, account2.iBan, "Piet Pietersen",
                                         1000d, "Geld");
    }

}