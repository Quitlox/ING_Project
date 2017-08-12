package honours.ing.banq.time;

import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.account.SavingsAccount;
import honours.ing.banq.transaction.Transaction;
import honours.ing.banq.transaction.TransactionRepository;
import honours.ing.banq.util.IBANUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
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

    @Autowired
    private TransactionRepository transactionRepository;

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        List<Time> times = timeRepository.findAll();
        if (times != null && times.size() == 0) {
            Time initialTime = new Time(new Date().getTime());
            timeRepository.save(initialTime);
        }
    }

    // 00:01, later than chargeBankAccountInterest()
    //@Scheduled(cron = "0 0 0 * * ?")
    public void calculateBankAccountInterest() {
        Calendar calendar = toCalendar(timeService.getDateObject());

        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            chargeBankAccountInterest();
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

    public void chargeBankAccountInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            bankAccount.subBalance(bankAccount.getBuiltInterest());
            bankAccount.resetBuiltInterest();
        }

        bankAccountRepository.save(bankAccounts);
    }

    public void calculateSavingsAccountInterest() {
        Calendar calendar = toCalendar(timeService.getDateObject());

        if (calendar.get(Calendar.MONTH) == 0 && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            chargeSavingsAccountInterest();
        }

        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        for (BankAccount bankAccount : bankAccounts) {
            SavingsAccount savingsAccount = bankAccount.getSavingsAccount();

            if (savingsAccount == null) {
                continue;
            }

            double interest = savingsAccount.getBalance() > 75000d ? 0.2d : 0.15d;
            savingsAccount.addBuiltInterest(-savingsAccount.getDailyLow() * (Math.pow(1d + interest, 1d /
                                                                                                     calendar.getActualMaximum(
                                                                                                             Calendar.DAY_OF_YEAR)) -
                                                                             1));
        }

        bankAccountRepository.save(bankAccounts);
    }

    public void chargeSavingsAccountInterest() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : bankAccounts) {
            SavingsAccount savingsAccount = bankAccount.getSavingsAccount();

            if (savingsAccount == null) {
                continue;
            }

            Transaction transaction = new Transaction(null, IBANUtil.generateIBAN(bankAccount), bankAccount.getPrimaryHolder().getName(), timeService.getDateObject(), savingsAccount.getBuiltInterest(), "Interest");
            transactions.add(transaction);

            savingsAccount.subBalance(savingsAccount.getBuiltInterest());
            savingsAccount.resetBuiltInterest();
        }

        bankAccountRepository.save(bankAccounts);
        transactionRepository.save(transactions);
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
