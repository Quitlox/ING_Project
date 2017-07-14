package honours.ing.banq.card;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.access.bean.NewCardBean;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthService;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.time.Time;
import honours.ing.banq.time.TimeService;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Kevin Witlox
 * @since 11-7-2017.
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class CardServiceImpl implements CardService {

    // Services
    @Autowired
    private AuthService authService;

    @Autowired
    private TimeService timeService;

    // Repositories
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public NewCardBean invalidateCard(String token, String iBan, String cardNumber,
                                      boolean newPin) throws InvalidParamValueError, NotAuthorizedError {
        Customer customer = authService.getAuthorizedCustomer(token);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(iBan));

        if (bankAccount == null) {
            throw new InvalidParamValueError("The given iBAN does not exist.");
        }

        Card authorizedCard = cardRepository.findByAccountAndHolderAndAndInvalidatedIsFalse(bankAccount, customer);
        Card requestedCard = cardRepository.findByCardNumber(cardNumber);

        if (authorizedCard == null) {
            throw new NotAuthorizedError();
        }

        if (requestedCard == null) {
            throw new InvalidParamValueError("The given cardNumber does not exist.");
        }

        if (!authorizedCard.equals(requestedCard)) {
            if (requestedCard.getAccount().equals(bankAccount)) {
                throw new InvalidParamValueError("The given card is already invalidated.");
            } else {
                throw new NotAuthorizedError();
            }
        }

        // Invalidate previous card
        authorizedCard.invalidate();
        cardRepository.save(authorizedCard);

        // Create new Card
        Card newCard;
        if (newPin) {
            newCard = new Card(token, customer, bankAccount, CardUtil.generateCardNumber(cardRepository), timeService.getDateObject());
        } else {
            newCard = new Card(token, customer, bankAccount, CardUtil.generateCardNumber(cardRepository), authorizedCard
                    .getPin(), timeService.getDateObject());
        }
        cardRepository.save(newCard);

        // Pay 7,50 for the card
        bankAccount.subBalance(7.5d);
        bankAccountRepository.save(bankAccount);

        if (newPin) {
            NewCardBean newCardBean = new NewCardBean(newCard);
            newCardBean.setPinCode(newCard.getPin());
            return newCardBean;
        } else {
            return new NewCardBean(newCard);
        }
    }

    @Override
    public void unblockCard(String authToken, String iBan,
                            String pinCard) throws InvalidParamValueError, NotAuthorizedError, NoEffectError {
        Customer customer = authService.getAuthorizedCustomer(authToken);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(iBan));
        Card card = cardRepository.findByAccountAndCardNumber(bankAccount, pinCard);

        if (bankAccount == null || card == null) {
            throw new InvalidParamValueError("The given iBAN/pinCard combination is invalid");
        }

        if ((!bankAccount.getPrimaryHolder().equals(customer)) && (!bankAccount.getHolders().contains(customer))) {
            throw new NotAuthorizedError();
        }

        if (!card.isBlocked()) {
            throw new NoEffectError();
        }

        card.unblock();
        cardRepository.save(card);
    }

}
