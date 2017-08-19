package honours.ing.banq.access;

import honours.ing.banq.BoilerplateTest;
import honours.ing.banq.InvalidParamValueError;
import honours.ing.banq.account.BankAccount;
import honours.ing.banq.account.BankAccountRepository;
import honours.ing.banq.account.bean.NewAccountBean;
import honours.ing.banq.auth.NotAuthorizedError;
import honours.ing.banq.info.bean.UserAccessBean;
import honours.ing.banq.util.IBANUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 * @since 19-8-2017.
 */
public class AccessServiceTest extends BoilerplateTest {

    // Repositories
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    public void provideAccess() throws Exception {
        BankAccount bankAccount1 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));
        BankAccount bankAccount2 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account2.iBan));

        List<UserAccessBean> accessBeans = infoService.getUserAccess(account2.token);
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount2, null)), is(true));

        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        accessBeans = infoService.getUserAccess(account2.token);
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount1, null)), is(true));
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount2, null)), is(true));
    }

    @Test(expected = InvalidParamValueError.class)
    public void provideAccessNonExistantUsername() throws Exception {
        accessService.provideAccess(account1.token, account1.iBan, "");
    }

    @Test
    public void provideAccessAsHolder() throws Exception {
        NewAccountBean newAccountBean = bankAccountService
                .openAccount("Test", "Test", "Test", "09-08-1998", "1234", "Test", "1234", "Test", "Test", "1234");
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        exception.expect(NotAuthorizedError.class);
        accessService.provideAccess(account2.token, account1.iBan, "Test");
    }

    @Test
    public void revokeAccess() throws Exception {
        BankAccount bankAccount1 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account1.iBan));
        BankAccount bankAccount2 = bankAccountRepository.findOne((int) IBANUtil.getAccountNumber(account2.iBan));

        List<UserAccessBean> accessBeans = infoService.getUserAccess(account2.token);
        assertThat(accessBeans.size(), equalTo(1));
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount2, null)), is(true));

        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        accessBeans = infoService.getUserAccess(account2.token);
        assertThat(accessBeans.size(), equalTo(2));
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount1, null)), is(true));
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount2, null)), is(true));

        accessService.revokeAccess(account1.token, account1.iBan, account2.username);
        accessBeans = infoService.getUserAccess(account2.token);
        assertThat(accessBeans.size(), equalTo(1));
        assertThat(accessBeans.contains(new UserAccessBean(bankAccount2, null)), is(true));
    }

    @Test(expected = InvalidParamValueError.class)
    public void revokeAccessNonExistantUsername() throws Exception {
        accessService.revokeAccess(account1.token, account1.iBan, "");
    }

    @Test(expected = InvalidParamValueError.class)
    public void revokeAccessSelf() throws Exception {
        accessService.revokeAccess(account1.token, account1.iBan, account1.username);
    }

    @Test(expected = NoEffectError.class)
    public void revokeAccessUsernameNotAHolder() throws Exception {
        accessService.revokeAccess(account1.token, account1.iBan, account2.username);
    }

    @Test
    public void revokeAccessAsHolder() throws Exception {
        NewAccountBean newAccountBean = bankAccountService
                .openAccount("Test", "Test", "Test", "09-08-1998", "1234", "Test", "1234", "Test", "Test", "1234");
        accessService.provideAccess(account1.token, account1.iBan, account2.username);
        accessService.provideAccess(account1.token, account1.iBan, "Test");

        exception.expect(NotAuthorizedError.class);
        accessService.revokeAccess(account2.token, account1.iBan, "Test");
    }

}