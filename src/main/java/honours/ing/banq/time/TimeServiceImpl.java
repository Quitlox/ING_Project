package honours.ing.banq.time;

import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.time.bean.DateBean;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionRepository;
import honours.ing.banq.util.IBANUtil;
import org.aspectj.apache.bcel.generic.FieldOrMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

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
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }

        if (nrOfDays <= 0) {
            throw new InvalidParamValueError("The nrOfDays should be positive.");
        }

        for (int i = 0; i < nrOfDays; i++) {
            simulateDay();
        }
    }

    private void simulateDay() {
        List<Time> times = timeRepository.findAll();

        Time time = times.get(0);
        time.setShift(time.getShift() + 1);
        timeRepository.save(time);

        // Simulate Interest
        timeManager.calculateInterest();
    }

    @Override
    public void reset() throws NoEffectError {
        // Delete previous entry
        List<Time> times = timeRepository.findAll();
        int shift = times.get(0).getShift();
        Calendar startDate = TimeManager.toCalendar(getDateObject());
        startDate.add(Calendar.DAY_OF_MONTH, -shift + 1);

        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        Calendar today = TimeManager.toCalendar(getDateObject());
        Calendar firstOfLastMonth = TimeManager.toCalendar(getDateObject());
        firstOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);

        double percentage = 0;
        while (today.after(startDate)) {
            double dailyRate = BankAccount.INTEREST_MONTHLY /
                               TimeManager.toCalendar(getDateObject()).getActualMaximum(Calendar.DAY_OF_MONTH);
            if (today.after(firstOfLastMonth)) {
                for (BankAccount bankAccount : bankAccounts) {
                    bankAccount.addBuiltInterest(bankAccount.getBalance() * dailyRate);
                }
            } else {
                percentage += dailyRate;
            }

            times.get(0).setShift(times.get(0).getShift() - 1);
            today = TimeManager.toCalendar(getDateObject());
        }

        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.subBalance(bankAccount.getBalance() - bankAccount.getBalance() / (1 + percentage));
        }

        // Save
        bankAccountRepository.save(bankAccounts);
        timeRepository.save(times);

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
    }

    private void resetDay() {
        List<Time> times = timeRepository.findAll();

        // Calculate Interest
        timeManager.calculateReverseInterest();

        Time time = times.get(0);
        time.setShift(time.getShift() - 1);
        timeRepository.save(time);

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
