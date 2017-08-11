package honours.ing.banq.access;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.bean.NewCardBean;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthService;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.card.Card;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.card.CardUtil;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.customer.CustomerRepository;
import honours.ing.banq.time.TimeService;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jeffrey
 * @since 31-5-17
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class AccessServiceImpl implements AccessService {

    // Services
    @Autowired
    private AuthService auth;

    @Autowired
    private TimeService timeService;

    // Repositories
    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public NewCardBean provideAccess(String authToken, String iBAN, String username) throws InvalidParamValueError, NotAuthorizedError, NoEffectError {
        Customer customer = auth.getAuthorizedCustomer(authToken);

        long accountNumber = IBANUtil.getAccountNumber(iBAN);
        BankAccount account = accountRepository.findOne((int) accountNumber);

        if (account == null) {
            throw new InvalidParamValueError("The given iBAN does not exist.");
        }

        if (!account.getPrimaryHolder().equals(customer)) {
            throw new NotAuthorizedError();
        }

        Customer holder = customerRepository.findByUsername(username);

        if (holder == null) {
            throw new InvalidParamValueError("The given username does not exist.");
        }

        if (account.getPrimaryHolder().equals(holder)) {
            throw new InvalidParamValueError("You can't provide access to yourself for your own account");
        }

        if (account.getHolders().contains(holder)) {
            throw new NoEffectError();
        }

        account.addHolder(holder);
        accountRepository.save(account);
        Card card = new Card(authToken, holder, account, CardUtil.generateCardNumber(cardRepository), timeService.getDateObject());
        cardRepository.save(card);

        return new NewCardBean(card);
    }

    @Override
    public void revokeAccess(String authToken, String iBAN, String username) throws InvalidParamValueError, NotAuthorizedError, NoEffectError {
        Customer customer = auth.getAuthorizedCustomer(authToken);

        long accountNumber = IBANUtil.getAccountNumber(iBAN);
        BankAccount account = accountRepository.findOne((int) accountNumber);

        if (!account.getPrimaryHolder().equals(customer)) {
            throw new NotAuthorizedError();
        }

        Customer holder = customerRepository.findByUsername(username);

        if (holder == null) {
            throw new InvalidParamValueError("The given username does not exist.");
        }

        if (account.getPrimaryHolder().equals(holder)) {
            throw new InvalidParamValueError("You can't revoke access from yourself for your own account");
        }

        if (!account.getHolders().contains(holder)) {
            throw new NoEffectError();
        }

        account.removeHolder(holder);
        accountRepository.save(account);
        Card card = cardRepository.findByAccountAndHolderAndAndInvalidatedIsFalse(account, holder);
        cardRepository.delete(card);
    }

}
