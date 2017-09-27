package honours.ing.banq.log.bean;

import honours.ing.banq.log.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 27-9-2017.
 */
public class LogBean {

    private String timeStamp;
    private String eventLog;

    private LogBean() {
    }

    public LogBean(Log log) {
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(log.getTimeStamp());
        this.eventLog = log.getEventLog();
    }

    public static List<LogBean> generate(List<Log> logs) {
        List<LogBean> res = new ArrayList<>(logs.size());
        for (Log log : logs) {
            res.add(new LogBean(log));
        }

        return res;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getEventLog() {
        return eventLog;
    }

}
