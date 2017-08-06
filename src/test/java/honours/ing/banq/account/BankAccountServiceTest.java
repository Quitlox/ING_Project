package honours.ing.banq.account;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.bean.NewAccountBean;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.util.IBANUtil;
import honours.ing.banq.util.StringUtil;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 * @since 5-8-17
 */
public class BankAccountServiceTest extends BoilerplateTest {

    @Test
    public void openAccount() throws Exception {
        String username = StringUtil.generate(10);
        String password = StringUtil.generate(10);

        NewAccountBean account = bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), username, password);

        // Get the auth token
        String token = authService.getAuthToken(username, password).getAuthToken();
        assertThat(token, notNullValue());

        // Check user access
        List<UserAccessBean> list = infoService.getUserAccess(token);
        assertThat(list.size(), equalTo(1));
        assertThat(list.get(0).getOwner(), equalTo("University of Twente"));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueSSN() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", account1.ssn,
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), StringUtil.generate(10), StringUtil.generate(10));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueEmail() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                account1.email, StringUtil.generate(10), StringUtil.generate(10));
    }

    @Test(expected = InvalidParamValueError.class)
    public void openAccountNonUniqueUsername() throws Exception {
        bankAccountService.openAccount(
                "University", "of Twente", "UT",
                "1996-1-1", StringUtil.generate(10),
                "Universiteitsstraat 1, Enschede", "06-12345678",
                StringUtil.generate(10), account1.username, StringUtil.generate(10));
    }

    @Test
    public void openAdditionalAccount() throws Exception {
        NewAccountBean account = bankAccountService.openAdditionalAccount(account1.token);

        List<UserAccessBean> list = infoService.getUserAccess(account1.token);
        assertThat(list.size(), equalTo(2));
    }

    @Test
    public void closeAccount() throws Exception {
        List<UserAccessBean> list = infoService.getUserAccess(account1.token);
        for (UserAccessBean userAccessBean : list) {
            bankAccountService.closeAccount(account1.token, userAccessBean.getiBan());
        }
    }

    @Test(expected = InvalidParamValueError.class)
    public void closeNonExistingAccount() throws Exception {
        bankAccountService.closeAccount(account1.token, IBANUtil.generateIBAN(123456789));
    }

    @Test
    public void setOverdraftLimit() throws Exception {
        bankAccountService.setOverdraftLimit(account1.token, account1.iBan, 1000d);

        //
    }

}