package honours.ing.banq.account;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.bean.NewAccountBean;
import honours.ing.banq.account.bean.OverdraftLimitBean;
import honours.ing.banq.auth.NotAuthorizedError;

/**
 * @author jeffrey
 * @since 17-4-17
 */
@JsonRpcService("/api/account")
public interface BankAccountService {

    /**
     * Open a bank account. This method also creates a <code>Customer</code> and a <code>Card</code> or this bank
     * account.
     *
     * @param name            the name of the customer
     * @param surname         the surname of the customer
     * @param initials        the initials of the customer
     * @param dob             the date of birth of the customer
     * @param ssn             the social security number of the customer
     * @param address         the address of the customer
     * @param telephoneNumber the telephone number of the customer
     * @param email           the email address of the customer
     * @param username        the preferred username of the customer
     * @param password        the password of the customer
     * @return an object containing basic information about the newly created bank account
     * @throws InvalidParamValueError if one or more of the parameters has an invalid value
     */
    NewAccountBean openAccount(
            @JsonRpcParam("name") String name, @JsonRpcParam("surname") String surname, @JsonRpcParam("initials")
            String initials,
            @JsonRpcParam("dob") String dob, @JsonRpcParam("ssn") String ssn,
            @JsonRpcParam("address") String address, @JsonRpcParam("telephoneNumber") String telephoneNumber,
            @JsonRpcParam("email") String email, @JsonRpcParam("username") String username, @JsonRpcParam("password")
                    String password)
            throws InvalidParamValueError;

    /**
     * Open a additional account for an existing logged in customer. Also creates a <code>Card</code> for this account.
     *
     * @param authToken the authentication token, obtained with <code>getAuthToken</code>
     * @return an object containing basic information about the newly created bank account
     * @throws NotAuthorizedError if something went wrong with the authentication module
     */
    NewAccountBean openAdditionalAccount(@JsonRpcParam("authToken") String authToken) throws NotAuthorizedError;

    void closeAccount(@JsonRpcParam("authToken") String authToken, @JsonRpcParam("iBAN") String iBAN) throws
            NotAuthorizedError, InvalidParamValueError;

    void setOverdraftLimit(@JsonRpcParam("authToken") String token, @JsonRpcParam("iBAN") String iBan,
                           @JsonRpcParam("overdraftLimit") Double overdraftLimit) throws InvalidParamValueError, NotAuthorizedError;

    OverdraftLimitBean getOverdraftLimit(@JsonRpcParam("authToken") String token,
                                         @JsonRpcParam("iBAN") String iBan) throws NotAuthorizedError;

    void openSavingsAccount(@JsonRpcParam("authToken") String token,
                            @JsonRpcParam("iBAN") String iBan) throws InvalidParamValueError, NotAuthorizedError;

    void closeSavingsAccount(@JsonRpcParam("authToken") String token,
                             @JsonRpcParam("iBAN") String iBan) throws InvalidParamValueError, NotAuthorizedError;

}