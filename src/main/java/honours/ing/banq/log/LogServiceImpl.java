package honours.ing.banq.log;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import honours.ing.banq.log.bean.LogBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    public List<LogBean> getEventLogs(String beginDate, String endDate) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date beginLong = null, endLong = null;
        try {
            beginLong = format.parse(beginDate);
            endLong = format.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return LogBean.generate(logRepository.findLogsByTimeStampBetween(beginLong, endLong));
    }

}
