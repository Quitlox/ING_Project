package honours.ing.banq.transaction;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.Account;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthService;
import honours.ing.banq.auth.InvalidPINError;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.card.Card;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.time.TimeRepository;
import honours.ing.banq.time.TimeService;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kevin Witlox
 * @since 4-6-2017
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class TransactionServiceImpl implements TransactionService {

    // TODO: Sanitize user input

    // Services
    @Autowired
    private AuthService auth;

    @Autowired
    private TimeService timeService;

    // Repositories
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Override
    public void depositIntoAccount(String iBAN, String pinCard, String pinCode, Double amount)
            throws InvalidParamValueError, InvalidPINError {
        if (!IBANUtil.isValidIBAN(iBAN)) {
            throw new InvalidParamValueError("The given IBAN is not valid.");
        }

        BankAccount bankAccount = auth.getAuthorizedAccount(iBAN, pinCard, pinCode);
        Card card = cardRepository.findByAccountAndCardNumber(bankAccount, pinCard);

        // Check if card is expired
        if (timeService.getDateObject().after(card.getExpirationDate())) {
            throw new InvalidParamValueError("The given card is expired.");
        }

        if (amount <= 0) {
            throw new InvalidParamValueError("The amount must be positive.");
        }

        // Update balance
        bankAccount.addBalance(amount);
        bankAccountRepository.save(bankAccount);

        // Save transaction
        Transaction transaction = new Transaction(null, iBAN, bankAccount.getPrimaryHolder()
                                                                         .getName(), timeService.getDateObject(),
                                                  amount, "Deposit");
        transactionRepository.save(transaction);
    }

    @Override
    public void payFromAccount(String sourceIBAN, String targetIBAN, String pinCard, String
            pinCode, Double amount) throws InvalidParamValueError, InvalidPINError {
        if (!IBANUtil.isValidIBAN(sourceIBAN)) {
            throw new InvalidParamValueError("The given source IBAN is not valid.");
        }

        BankAccount fromBankAccount = auth.getAuthorizedAccount(sourceIBAN, pinCard, pinCode);
        BankAccount toBankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber
                (targetIBAN));

        // Check if card is expired
        Card card = cardRepository.findByAccountAndCardNumber(fromBankAccount, pinCard);
        if (timeService.getDateObject().after(card.getExpirationDate())) {
            throw new InvalidParamValueError("The given card is expired.");
        }

        // Check balance
        if (fromBankAccount.getBalance() - amount < -fromBankAccount.getOverdraftLimit()) {
            throw new InvalidParamValueError("Not enough balance on account.");
        }

        if (amount <= 0) {
            throw new InvalidParamValueError("Amount should be greater than 0.");
        }

        // Update balance
        fromBankAccount.subBalance(amount);
        toBankAccount.addBalance(amount);
        bankAccountRepository.save(fromBankAccount);
        bankAccountRepository.save(toBankAccount);

        // Save Transaction
        Transaction transaction = new Transaction(sourceIBAN, targetIBAN, toBankAccount
                .getPrimaryHolder().getName(), timeService.getDateObject(), amount, "Payment with debit card.");
        transactionRepository.save(transaction);
    }

    @Override
    public void transferMoney(String authToken, String sourceIBAN, String targetIBAN, String
            targetName, Double amount, String description) throws InvalidParamValueError,
            NotAuthorizedError {
        boolean sourceIsSavingsAccount = IBANUtil.isSavingsAccount(sourceIBAN);
        sourceIBAN = IBANUtil.convertToBankAccount(sourceIBAN);
        boolean targetIsSavingsAccount = IBANUtil.isSavingsAccount(targetIBAN);
        targetIBAN = IBANUtil.convertToBankAccount(targetIBAN);

        if (!IBANUtil.isValidIBAN(sourceIBAN) || !IBANUtil.isValidIBAN(targetIBAN)) {
            throw new InvalidParamValueError("One of the given IBANs is invalid.");
        }

        BankAccount sourceBankAccount = bankAccountRepository.findOne((int) IBANUtil
                .getAccountNumber(sourceIBAN));
        BankAccount targetBankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber
                (targetIBAN));
        Customer customer = auth.getAuthorizedCustomer(authToken);

        // Check if bank account is held by customer
        if (!sourceBankAccount.getHolders().contains(customer) && !sourceBankAccount.getPrimaryHolder
                ().equals(customer)) {
            throw new NotAuthorizedError();
        }

        // Check amount
        if (amount <= 0) {
            throw new InvalidParamValueError("Amount should be greater than 0.");
        }

        Account sourceAccount = sourceBankAccount;
        Account targetAccount = targetBankAccount;

        // Check if SavingsAccount Transaction
        if (sourceIsSavingsAccount ^ targetIsSavingsAccount) {
            if (!sourceBankAccount.equals(targetBankAccount)) {
                throw new InvalidParamValueError(
                        "A SavingsAccount can only exchange currency with its holder BankAccount.");
            }
        } else if (sourceIsSavingsAccount & targetIsSavingsAccount) {
            throw new InvalidParamValueError(
                    "A SavingsAccount can only exchange currency with its holder BankAccount.");
        }

        // Update Source/Destination if necessary
        if (sourceIsSavingsAccount) {
            sourceAccount = sourceBankAccount.getSavingsAccount();
            sourceIBAN += "S";
        } else if (targetIsSavingsAccount) {
            targetAccount = targetBankAccount.getSavingsAccount();
            targetIBAN += "S";

            if (targetAccount == null) {
                throw new InvalidParamValueError("The given BankAccount has not opened a SavingsAccount.");
            }
        }

        // Check balance
        if (sourceAccount.getBalance() - amount < -sourceAccount.getOverdraftLimit()) {
            throw new InvalidParamValueError("Not enough balance on account.");
        }

        // Update balance
        sourceAccount.subBalance(amount);
        targetAccount.addBalance(amount);
        bankAccountRepository.save(sourceBankAccount);
        bankAccountRepository.save(targetBankAccount);

        Transaction transaction = new Transaction(sourceIBAN, targetIBAN, targetName, timeService.getDateObject(),
                                                  amount, description);
        transactionRepository.save(transaction);
    }

}
