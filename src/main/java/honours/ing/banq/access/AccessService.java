package honours.ing.banq.access;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.bean.NewCardBean;
import honours.ing.banq.auth.NotAuthorizedError;

/**
 * @author jeffrey
 * @since 31-5-17
 */
@JsonRpcService("/api/access")
public interface AccessService {

    NewCardBean provideAccess(
            @JsonRpcParam("authToken") String authToken, @JsonRpcParam("iBAN") String iBAN,
            @JsonRpcParam("username") String username)
            throws InvalidParamValueError, NotAuthorizedError, NoEffectError;

    void revokeAccess(
            @JsonRpcParam("authToken") String authToken, @JsonRpcParam("iBAN") String iBAN,
            @JsonRpcParam("username") String username)
            throws InvalidParamValueError, NotAuthorizedError, NoEffectError;


}
