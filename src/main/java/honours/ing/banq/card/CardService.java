package honours.ing.banq.card;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.access.NoEffectError;
import honours.ing.banq.access.bean.NewCardBean;
import honours.ing.banq.auth.NotAuthorizedError;

/**
 * @author kevin
 * @since 11-7-2017.
 */
@JsonRpcService("/api/card")
public interface CardService {

    NewCardBean invalidateCard(@JsonRpcParam("authToken") String token, @JsonRpcParam("iBAN") String iBan,
                               @JsonRpcParam("pinCard") String cardNumber,
                               @JsonRpcParam("newPin") boolean newPin) throws InvalidParamValueError, NotAuthorizedError;

    void unblockCard(@JsonRpcParam("authToken") String authToken, @JsonRpcParam("iBAN") String iBan,
                     @JsonRpcParam("pinCard") String pinCard) throws InvalidParamValueError, NotAuthorizedError, NoEffectError;

}
