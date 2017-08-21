package honours.ing.banq.auth;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@Component
@AutoJsonRpcServiceImpl
public class AuthServiceAPIImpl implements AuthServiceAPI {

    @Autowired
    private AuthService authService;

    @Override
    public AuthToken getAuthToken(String username, String password) throws AuthenticationError {
        return authService.getAuthToken(username, password);
    }

}
