package honours.ing.banq.time;

import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthRepository;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.CustomerRepository;
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
    private AuthRepository authRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Fields
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
        // TODO: replace with TRUNCATE, which is faster
        authRepository.deleteAll();
        bankAccountRepository.deleteAll();
        cardRepository.deleteAll();
        transactionRepository.deleteAll();
        customerRepository.deleteAll();

        timeRepository.findAll().get(0).setShift(0);
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
