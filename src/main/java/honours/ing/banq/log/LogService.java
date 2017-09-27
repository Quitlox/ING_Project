package honours.ing.banq.log;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@JsonRpcService("/api/log")
public interface LogService {

    List<Log> getEventLogs(@JsonRpcParam("beginDate") String beginDate, @JsonRpcParam("endDate") String endDate);

}
