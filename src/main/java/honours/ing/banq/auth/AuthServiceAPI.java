package honours.ing.banq.auth;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 * @author Kevin Witlox
 * @since 21-8-2017.
 */
@JsonRpcService("/api/auth")
public interface AuthServiceAPI {

    AuthToken getAuthToken(@JsonRpcParam("username") String username,
                           @JsonRpcParam("password") String password) throws AuthenticationError;

}
