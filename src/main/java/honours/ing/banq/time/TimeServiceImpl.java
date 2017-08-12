package honours.ing.banq.time;

import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.auth.AuthRepository;
import honours.ing.banq.card.CardRepository;
import honours.ing.banq.customer.CustomerRepository;
import honours.ing.banq.time.bean.DateBean;
import honours.ing.banq.transaction.TransactionRepository;
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
        // Delete previous
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time.getUtc()));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        time.setUtc(calendar.getTime().getTime());
        timeRepository.save(time);

        // Simulate Interest
        timeManager.calculateBankAccountInterest();
        timeManager.calculateSavingsAccountInterest();
    }

    @Override
    public void reset() throws NoEffectError {
        // TODO: replace with TRUNCATE, which is faster
        authRepository.deleteAll();
        bankAccountRepository.deleteAll();
        cardRepository.deleteAll();
        transactionRepository.deleteAll();
        customerRepository.deleteAll();

        timeRepository.findAll().get(0).setUtc(new Date().getTime());
    }

    @Override
    public DateBean getDate() {
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }


        Calendar serverCalendar = Calendar.getInstance();
        serverCalendar.setTime(new Date(times.get(0).getUtc()));

        // Only change date, not time
        Calendar systemCalendar = Calendar.getInstance();
        systemCalendar.set(serverCalendar.get(Calendar.YEAR), serverCalendar.get(Calendar.MONTH), serverCalendar.get(Calendar.DAY_OF_MONTH));
        return new DateBean(systemCalendar.getTime());
    }

    @Override
    public Date getDateObject() {
        List<Time> times = timeRepository.findAll();
        if (times.size() != 1) {
            throw new IllegalStateException("There should only be one time entry in the database.");
        }

        Calendar serverCalendar = Calendar.getInstance();
        serverCalendar.setTime(new Date(times.get(0).getUtc()));

        // Only change date, not time
        Calendar systemCalendar = Calendar.getInstance();
        systemCalendar.set(serverCalendar.get(Calendar.YEAR), serverCalendar.get(Calendar.MONTH), serverCalendar.get(Calendar.DAY_OF_MONTH));
        return systemCalendar.getTime();
    }

}
