package honours.ing.banq.transaction;

import honours.ing.banq.account.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author jeffrey
 * @since 24-4-17
 */
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {



}