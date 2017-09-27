package honours.ing.banq.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
public interface LogRepository extends JpaRepository<Log, Integer> {

    List<Log> findLogsByTimeStampBetween(Date beginDate, Date endDate);

}
