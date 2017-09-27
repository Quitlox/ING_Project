package honours.ing.banq.config;

import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.auth.AuthenticationError;
import honours.ing.banq.auth.InvalidPINError;
import honours.ing.banq.auth.NotAuthorizedError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jeffrey
 * @since 17-4-17
 */
@Configuration
public class JsonRpcConfig {

    @Bean
    public static AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter() {
        AutoJsonRpcServiceImplExporter exp = new AutoJsonRpcServiceImplExporter();
        exp.setRethrowExceptions(false);
        exp.setShouldLogInvocationErrors(false);
        exp.setRegisterTraceInterceptor(false);
        exp.setErrorResolver((throwable, method, list) -> {
            int code = 500;
            if (throwable instanceof InvalidParamValueError) {
                code = 418;
            } else if (throwable instanceof NotAuthorizedError) {
                code = 419;
            } else if (throwable instanceof NoEffectError) {
                code = 420;
            } else if (throwable instanceof InvalidPINError) {
                code = 421;
            } else if (throwable instanceof AuthenticationError) {
                code = 422;
            }

            return new ErrorResolver.JsonError(code, throwable.getMessage(), null);
        });

        // exp.setHttpStatusCodeProvider();
        // exp.setErrorResolver();
        return exp;
    }

}
