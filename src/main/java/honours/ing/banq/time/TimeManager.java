package honours.ing.banq.time;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
@Component
public class TimeManager {

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        List<Time> times = timeRepository.findAll();
        if (times != null && times.size() == 0) {
            Time initialTime = new Time(0);
            timeRepository.save(initialTime);
        }
    }

    // 00:01, later than chargeInterest()
    @Scheduled(cron = "0 1 0 * * ?")
    public void calculateInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccount.getBalance() < 0) {
                bankAccount.addBuiltInterest(
                        bankAccount.getBalance() * (BankAccount.INTEREST_MONTHLY / GregorianCalendar.getInstance()
                                                                                                    .getActualMaximum(Calendar.DAY_OF_MONTH)));
            }
        }
    }

    @Scheduled(cron = "0 0 0 1 1/1 ? *")
    public void chargeInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.addBalance(bankAccount.getBuiltInterest());
            bankAccount.resetBuiltInterest();
        }
    }

}
