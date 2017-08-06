package honours.ing.banq.time;

import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.time.bean.DateBean;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionRepository;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
@Service
@Transactional
public class TimeServiceImpl implements TimeService {

    // Repositories
    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TimeManager timeManager;

    @Override
    public void simulateTime(int nrOfDays) throws InvalidParamValueError {
        // Delete previous
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }

        if (nrOfDays <= 0) {
            throw new InvalidParamValueError("The nrOfDays should be positive.");
        }

        Time time = times.get(0);
        time.setShift(time.getShift() + nrOfDays);
        timeRepository.save(time);

        // Simulate Interest
        timeManager.calculateInterest(BankAccount.INTEREST_MONTHLY, nrOfDays);
        timeManager.calculateInterest();
    }

    @Override
    public void reset() throws NoEffectError {
        // Delete previous entry
        List<Time> times = timeRepository.findAll();
        int shift = times.get(0).getShift();
        timeRepository.delete(times);
        // Reset time to current time
        Time time = new Time(0);
        timeRepository.save(time);

        // Revert transactions
        List<Transaction> futureTransactions = transactionRepository.findAllByDateAfter(getDateObject());
        for (Transaction transaction : futureTransactions) {
            if (transaction.getSource() != null) {
                BankAccount source = bankAccountRepository.findOne(
                        (int) IBANUtil.getAccountNumber(transaction.getSource()));
                source.addBalance(transaction.getAmount());
                bankAccountRepository.save(source);
            }

            BankAccount destination = bankAccountRepository.findOne(
                    (int) IBANUtil.getAccountNumber(transaction.getDestination()));
            destination.subBalance(transaction.getAmount());
            bankAccountRepository.save(destination);
        }
        transactionRepository.delete(futureTransactions);

        // Revert Interest
        timeManager.calculateInterest(1 / BankAccount.INTEREST_MONTHLY, shift);
        timeManager.calculateInterest();
    }

    @Override
    public DateBean getDate() {
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, times.get(0).getShift());
        return new DateBean(calendar.getTime());
    }

    @Override
    public Date getDateObject() {
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, times.get(0).getShift());
        return calendar.getTime();
    }

}
