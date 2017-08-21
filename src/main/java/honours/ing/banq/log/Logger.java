package honours.ing.banq.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@Aspect
@Component
public class Logger {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(* *.*(..))")
    protected void allMethod() {
    }

    @Pointcut("within(@com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl *)")
    protected void service() {
    }

    @After("allMethod() && service()")
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
    }

    @AfterThrowing(pointcut = "service() && allMethod()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        StringBuilder builder = new StringBuilder(exception.getClass().getSimpleName());
        builder.append("(").append(exception.getMessage()).append(")");
        builder.append(" thrown by ").append(joinPoint.getSignature().getName());
        logger.error(builder.toString());
    }


}
