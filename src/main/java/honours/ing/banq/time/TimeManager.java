package honours.ing.banq.time;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Kevin Witlox
 * @since 13-7-2017.
 */
@Component
public class TimeManager {

    // Services
    @Autowired
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
        Calendar calendar = toCalendar(timeService.getDateObject());

        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            chargeInterest();
        }

        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccount.getBalance() < 0) {
                bankAccount.addBuiltInterest(-bankAccount.getDailyLow() *
                                             (BankAccount.INTEREST_MONTHLY /
                                              calendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
            }
        }

        bankAccountRepository.save(bankAccounts);
    }

    public void calculateReverseInterest() {
        Calendar calendar = toCalendar(timeService.getDateObject());

        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccount.getBalance() < 0) {
                bankAccount.addBuiltInterest(-((bankAccount.getDailyLow() + bankAccount.getBuiltInterest()) / ((BankAccount.INTEREST_MONTHLY /
                                                                            calendar.getActualMaximum(
                                                                                    Calendar.DAY_OF_MONTH)) + 1) - (bankAccount.getDailyLow() + bankAccount.getBuiltInterest())));
            }
        }

        bankAccountRepository.save(bankAccounts);
    }

    public void chargeInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.subBalance(bankAccount.getBuiltInterest());
            bankAccount.resetBuiltInterest();
        }

        bankAccountRepository.save(bankAccounts);
    }

    public static GregorianCalendar toCalendar(Date date) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static long daysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }

    public static int monthsBetween(Calendar startCalendar, Calendar endCalendar) {
        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        return diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
    }

}
