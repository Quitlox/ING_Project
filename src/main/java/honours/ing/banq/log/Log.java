package honours.ing.banq.log;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Date timeStamp;
    private String eventLog;

    protected Log() {
    }

    public Log(Date timeStamp, String eventLog) {
        this.timeStamp = timeStamp;
        this.eventLog = eventLog;
    }

    public int getId() {
        return id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getEventLog() {
        return eventLog;
    }

    public void setEventLog(String eventLog) {
        this.eventLog = eventLog;
    }

}
