package honours.ing.banq.auth;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.card.Card;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.customer.CustomerRepository;
import honours.ing.banq.time.TimeService;
import honours.ing.banq.util.IBANUtil;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * @author jeffrey
 * @since 14-5-17
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final int TOKEN_LENGTH = 255;
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_=+[{]}|;:/?.>,<!@#$%&*()";

    private static final int VALIDITY = 1; // one day

    @Autowired
    private AuthRepository repository;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TimeService timeService;

    private Random random = new Random();

    @Override
    public AuthToken getAuthToken(String username, String password) throws AuthenticationError {
        Customer customer = customerRepository.findByUsernameAndPassword(username, password);
        if (customer == null) {
            throw new AuthenticationError();
        }

        Authentication auth = null;

        Date date = timeService.getDateObject();
        Calendar exp = Calendar.getInstance();
        exp.setTime(date);
        exp.add(Calendar.DAY_OF_MONTH, VALIDITY);

        while (auth == null) {
            try {
                auth = new Authentication(customer, genToken(), exp.getTime());
                repository.save(auth);
            } catch (Exception e) {
                auth = null;
            }
        }

        return new AuthToken(auth.getToken());
    }

    @Override
    public Customer getAuthorizedCustomer(String token) throws NotAuthorizedError {
        if (token == null || token.length() == 0) {
            throw new InvalidParamValueError("Token cannot be null/empty");
        }

        Authentication auth = repository.findByToken(token);
        if (auth == null || auth.hasExpired(timeService.getDateObject())) {
            throw new NotAuthorizedError();
        }

        return auth.getCustomer();
    }

    @Override
    public BankAccount getAuthorizedAccount(String iBAN, String pinCard,
                                            String pinCode) throws InvalidPINError, InvalidParamValueError {
        if (iBAN == null || iBAN.length() <= 8 || pinCard == null || pinCode == null) {
            throw new InvalidParamValueError("One of the parameters is null or the IBAN is not long enough");
        }
        BankAccount account = accountRepository.findOne((int) IBANUtil.getAccountNumber(iBAN));
        if (account == null) {
            throw new InvalidParamValueError("Account with the given IBAN does not exist in this system");
        }

        Card card = cardRepository.findByAccountAndCardNumber(account, pinCard);
        if (card == null) {
            throw new InvalidParamValueError("Card does not exist");
        }

        // Check expiration
        if (timeService.getDateObject().getTime() >= card.getExpirationDate().getTime()) {
            throw new InvalidPINError("The given card is expired.");
        }

        // Check block
        if (card.isBlocked()) {
            throw new InvalidPINError("The given card is blocked because of too many consecutive failed authorization attempts.");
        }

        // Check pin, track attempts
        if (!card.getPin().equals(pinCode)) {
            card.addAttempt();
            cardRepository.save(card);
            throw new InvalidPINError("The entered PIN is wrong.");
        }

        if (card.getAttempts() != 0) {
            card.resetAttempts();
            cardRepository.save(card);
        }

        return account;
    }

    private String genToken() {
        char[] res = new char[TOKEN_LENGTH];

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            res[i] = CHARS.charAt(random.nextInt(CHARS.length()));
        }

        return new String(res);
    }

}
