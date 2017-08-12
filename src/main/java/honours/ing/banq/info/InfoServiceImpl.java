package honours.ing.banq.info;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthService;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.customer.Customer;
import honours.ing.banq.info.bean.BalanceBean;
import honours.ing.banq.info.bean.BankAccountAccessBean;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionRepository;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Witlox
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class InfoServiceImpl implements InfoService {

    // Services
    @Autowired
    private AuthService auth;

    // Repositories
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public BalanceBean getBalance(String autToken, String iBAN) throws InvalidParamValueError,
            NotAuthorizedError {
        Customer customer = auth.getAuthorizedCustomer(autToken);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber
                (iBAN));

        if (bankAccount == null) {
            throw new InvalidParamValueError("The given IBAN does not exist.");
        }

        if (!bankAccount.getHolders().contains(customer) && !bankAccount.getPrimaryHolder().equals(customer)) {
            throw new NotAuthorizedError();
        }

        return new BalanceBean(bankAccount);
    }

    @Override
    public List<Transaction> getTransactionsOverview(String authToken, String iBAN, Integer
            nrOfTransactions) throws InvalidParamValueError, NotAuthorizedError {
        Customer customer = auth.getAuthorizedCustomer(authToken);
        BankAccount bankAccount = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber
                (iBAN));

        if (bankAccount == null) {
            throw new InvalidParamValueError("The given IBAN does not exist.");
        }

        if (nrOfTransactions <= 0) {
            throw new InvalidParamValueError("The number of transactions should be positive.");
        }

        if (!bankAccount.getHolders().contains(customer) && !bankAccount.getPrimaryHolder().equals(customer)) {
            throw new NotAuthorizedError();
        }

        List<Transaction> list = transactionRepository.findBySourceOrDestinationOrderByDateDesc(iBAN, iBAN);
        return list.size() > nrOfTransactions ? list.subList(0, nrOfTransactions) : transactionRepository
                .findBySourceOrDestinationOrderByDateDesc(iBAN,
                                                          iBAN);
    }

    @Override
    public List<UserAccessBean> getUserAccess(String authToken) throws NotAuthorizedError {
        Customer customer = auth.getAuthorizedCustomer(authToken);

        if (customer == null) {
            throw new NotAuthorizedError();
        }

        List<BankAccount> accounts = bankAccountRepository.findBankAccountsByHolders(customer.getId());
        List<BankAccount> primaryAccounts = bankAccountRepository.findBankAccountsByPrimaryHolder(customer);

        List<UserAccessBean> userAccessBeanList = new ArrayList<>();
        for (BankAccount account : accounts) {
            userAccessBeanList.add(new UserAccessBean(account, account.getPrimaryHolder()));
            if (account.getSavingsAccount() != null) {
                userAccessBeanList.add(new UserAccessBean(account.getSavingsAccount(), account.getPrimaryHolder()));
            }
        }

        for (BankAccount account : primaryAccounts) {
            userAccessBeanList.add(new UserAccessBean(account, account.getPrimaryHolder()));
            if (account.getSavingsAccount() != null) {
                userAccessBeanList.add(new UserAccessBean(account.getSavingsAccount(), account.getPrimaryHolder()));
            }
        }

        return userAccessBeanList;
    }

    @Override
    public List<BankAccountAccessBean> getBankAccountAccess(String authToken, String iBAN) throws
            InvalidParamValueError, NotAuthorizedError {
        iBAN = IBANUtil.convertToBankAccount(iBAN);

        Customer customer = auth.getAuthorizedCustomer(authToken);
        long accountNumber = IBANUtil.getAccountNumber(iBAN);
        BankAccount bankAccount = bankAccountRepository.findOne((int) accountNumber);

        if (!bankAccount.getPrimaryHolder().equals(customer)) {
            throw new NotAuthorizedError();
        }

        // Add all account holders
        List<BankAccountAccessBean> bankAccountAccessBeanList = new ArrayList<>();
        for (Customer holder : bankAccount.getHolders()) {
            bankAccountAccessBeanList.add(new BankAccountAccessBean(holder));
        }

        // Add primary holder
        bankAccountAccessBeanList.add(new BankAccountAccessBean(customer));

        return bankAccountAccessBeanList;
    }

}
