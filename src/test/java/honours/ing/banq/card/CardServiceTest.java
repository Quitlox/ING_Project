package honours.ing.banq.card;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.access.bean.NewCardBean;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.InvalidPINError;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.util.IBANUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author Kevin Witlox
 * @since 12-7-2017.
 */
public class CardServiceTest extends BoilerplateTest {

    // Repositories
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    public void invalidateCard() throws Exception {
        NewCardBean newCardBean;

        assertThat(cardRepository.findByCardNumber(account1.cardNumber).isInvalidated(), is(false));

        // NewPin false
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, account1.pin, 15d);
        newCardBean = cardService.invalidateCard(account1.token, account1.iBan, account1.cardNumber, false);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(7.5d));
        assertThat(cardRepository.findByCardNumber(account1.cardNumber).isInvalidated(), equalTo(true));
        assertThat(cardRepository.findByCardNumber(newCardBean.getPinCard()).isInvalidated(), equalTo(false));
        assertThat(cardRepository.findByCardNumber(newCardBean.getPinCard()).getPin(), equalTo(account1.pin));

        account1.cardNumber = newCardBean.getPinCard();

        // NewPin true
        newCardBean = cardService.invalidateCard(account1.token, account1.iBan, account1.cardNumber, true);
        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo(0d));
        assertThat(cardRepository.findByCardNumber(account1.cardNumber).isInvalidated(), equalTo(true));
        assertThat(cardRepository.findByCardNumber(newCardBean.getPinCard()).isInvalidated(), equalTo(false));
        assertThat(cardRepository.findByCardNumber(newCardBean.getPinCard()).getPin(),
                   equalTo(newCardBean.getPinCode()));

    }

    @Test(expected = InvalidParamValueError.class)
    public void invalidateCardInvalidIBAN() throws Exception {
        cardService.invalidateCard(account1.token, "null", account1.cardNumber, false);
    }

    @Test(expected = InvalidParamValueError.class)
    public void invalidateCardInvalidCardNumber() throws Exception {
        cardService.invalidateCard(account1.token, account1.iBan, "null", false);
    }

    @Test(expected = NotAuthorizedError.class)
    public void invalidateCardInvalidToken() throws Exception {
        cardService.invalidateCard("null", account1.iBan, account1.cardNumber, false);
    }

    @Test(expected = NotAuthorizedError.class)
    public void invalidateCardWrongToken() throws Exception {
        cardService.invalidateCard(account1.token, account2.iBan, account2.cardNumber, false);
    }

    @Test(expected = NotAuthorizedError.class)
    public void invalidateCardWrongCardNumber() throws Exception {
        cardService.invalidateCard(account1.token, account1.iBan, account2.cardNumber, false);
    }

    @Test(expected = NotAuthorizedError.class)
    public void invalidateCardWrongIBAN() throws Exception {
        cardService.invalidateCard(account1.token, account2.iBan, account1.cardNumber, false);
    }

    @Test(expected = InvalidParamValueError.class)
    public void invalidateCardAlreadyInvalidated() throws Exception {
        cardService.invalidateCard(account1.token, account1.iBan, account1.cardNumber, false);
        cardService.invalidateCard(account1.token, account1.iBan, account1.cardNumber, false);
    }

    @Test
    public void unblockCard() throws Exception {
        Card card = cardRepository.findByAccountAndCardNumber(
                bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan)), account1.cardNumber);
        assertThat(card.isBlocked(), is(false));

        // Enter wrong pin three times
        try {
            transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, "0000", 200d);
        } catch (Exception e) {}
        assertThat(card.isBlocked(), is(false));
        try {
            transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        } catch (Exception e) {}
        assertThat(card.isBlocked(), is(false));
        try {
            transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        } catch (Exception e) {}
        assertThat(card.isBlocked(), is(true));

        // Unblock
        cardService.unblockCard(account1.token, account1.iBan, account1.cardNumber);
        assertThat(card.isBlocked(), is(false));
    }

    @Test(expected = InvalidPINError.class)
    public void unblockCardBlockedCardPayFromAccount() throws Exception {
        Card card = cardRepository.findByAccountAndCardNumber(
                bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan)), account1.cardNumber);
        assertThat(card.isBlocked(), is(false));

        // Enter wrong pin three times
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, "0000", 200d);
        assertThat(card.isBlocked(), is(false));
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        assertThat(card.isBlocked(), is(false));
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        assertThat(card.isBlocked(), is(true));

        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
    }

    @Test(expected = InvalidPINError.class)
    public void unblockCardBlockedCardDepositIntoAccount() throws Exception {
        Card card = cardRepository.findByAccountAndCardNumber(
                bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan)), account1.cardNumber);
        assertThat(card.isBlocked(), is(false));

        // Enter wrong pin three times
        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, "0000", 200d);
        assertThat(card.isBlocked(), is(false));
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        assertThat(card.isBlocked(), is(false));
        transactionService.payFromAccount(account1.iBan, account2.iBan, account1.cardNumber, "0000", 100d);
        assertThat(card.isBlocked(), is(true));

        transactionService.depositIntoAccount(account1.iBan, account1.cardNumber, "0000", 100d);
    }

    @Test(expected = NoEffectError.class)
    public void unblockCardNoEffect() throws Exception {
        Card card = cardRepository.findByAccountAndCardNumber(
                bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan)), account1.cardNumber);
        assertThat(card.isBlocked(), is(false));

        cardService.unblockCard(account1.token, account1.iBan, account1.cardNumber);
    }

    @Test(expected = InvalidParamValueError.class)
    public void unblockCardWrongIBAN() throws Exception {
        cardService.unblockCard(account1.token, account2.iBan, account1.cardNumber);
    }

    @Test(expected = NotAuthorizedError.class)
    public void unblockCardWrongToken() throws Exception {
        cardService.unblockCard(account1.token, account2.iBan, account2.cardNumber);
    }

    @Test(expected = InvalidParamValueError.class)
    public void unblockCardWrongCardNumber() throws Exception {
        cardService.unblockCard(account1.token, account1.iBan, account2.cardNumber);
    }

    @Test(expected = InvalidParamValueError.class)
    public void unblockCardInvalidIBAN() throws Exception {
        cardService.unblockCard(account1.token, "-1", account1.cardNumber);
    }

    @Test(expected = InvalidParamValueError.class)
    public void unblockCardInvalidCardNumber() throws Exception {
        cardService.unblockCard(account1.token, account1.iBan, "-1");
    }

}