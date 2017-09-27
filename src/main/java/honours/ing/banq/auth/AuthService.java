package honours.ing.banq.auth;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.customer.Customer;

/**
 * @author jeffrey
 * @since 14-5-17
 */
public interface AuthService extends AuthServiceAPI {

    Customer getAuthorizedCustomer(String token) throws NotAuthorizedError;

    BankAccount getAuthorizedAccount(String iBAN, String pinCard,
                                     String pinCode) throws InvalidPINError, InvalidParamValueError;

}
