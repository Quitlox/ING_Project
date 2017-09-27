package honours.ing.banq.log;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@Service
@AutoJsonRpcServiceImpl
@Transactional
public class LogServiceImpl implements LogService {

    @Autowired
    private LogRepository logRepository;

    @Override
    public List<Log> getEventLogs(String beginDate, String endDate) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        long beginLong = 0, endLong = 0;
        try {
            beginLong = format.parse(beginDate).getTime();
            endLong = format.parse(endDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return logRepository.findLogsByTimeStampBetween(beginLong, endLong);
    }

}
