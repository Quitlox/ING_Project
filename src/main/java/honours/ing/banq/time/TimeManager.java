package honours.ing.banq.time;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
@Component
public class TimeManager {

    // Services
    private TimeService timeService;

    // Repositories
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
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateInterest() {
        if (toCalendar(timeService.getDateObject()).get(Calendar.DAY_OF_MONTH) == 1) {
            chargeInterest();
        }

        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccount.getBalance() < 0) {
                bankAccount.addBuiltInterest(
                        bankAccount.getBalance() * (BankAccount.INTEREST_MONTHLY / GregorianCalendar.getInstance()
                                                                                                    .getActualMaximum(
                                                                                                            Calendar.DAY_OF_MONTH)));
            }
        }
    }

    public void calculateInterest(double monthlyInterest, double days) {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccount.getBalance() < 0) {
                for (int i = 0; i < days; i++) {
                    bankAccount.addBuiltInterest(bankAccount.getBalance() * (monthlyInterest /
                                                                             toCalendar(timeService.getDateObject())
                                                                                     .getActualMaximum(
                                                                                             Calendar.DAY_OF_MONTH)));
                }
            }
        }
    }

    public void chargeInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.addBalance(bankAccount.getBuiltInterest());
            bankAccount.resetBuiltInterest();
        }
    }

    private static GregorianCalendar toCalendar(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

}
