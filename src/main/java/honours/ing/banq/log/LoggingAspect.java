package honours.ing.banq.log;

import honours.ing.banq.time.TimeService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Repositories
    @Autowired
    private LogRepository logRepository;

    // Services
    @Autowired
    private TimeService timeService;

    @Pointcut("execution(* *.*(..))")
    protected void allMethod() {
    }

    @Pointcut("within(@com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl *)")
    protected void service() {
    }

    @Before("allMethod() && service()")
    public void logAfter(JoinPoint point) {
        StringBuilder builder = new StringBuilder(point.getSignature().getName());
        builder.append("(");

        Object[] args = point.getArgs();
        for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            builder.append(object.toString().length() == 255 ? "token" : object.toString());

            if (i + 1 < args.length) {
                builder.append(", ");
            }
        }

        builder.append(");");
        logger.info(builder.toString());
        saveLog(builder.toString());
    }

    @AfterThrowing(pointcut = "service() && allMethod()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String message = exception.getClass().getSimpleName() + "(" + exception.getMessage() + ")" +
                         " thrown by " + joinPoint.getSignature().getName();
        logger.error(message);
        saveLog("error");
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private void saveLog(String message) {
        //logRepository.save(new Log(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()), message));
        logRepository.save(new Log(timeService.getDateObject().getTime(), message));
    }

}
