package honours.ing.banq;

import honours.ing.banq.account.bean.NewAccountBean;
import honours.ing.banq.bean.AccountInfo;

/**
 * @author Kevin Witlox
 * @since 5-8-2017.
 */
public class TestAccountInfo extends AccountInfo {

    public String email, ssn;

    public TestAccountInfo(NewAccountBean bean, String username, String password, String email, String ssn) {
        super(bean, username, password);

        this.email = email;
        this.ssn = ssn;
    }

    public TestAccountInfo(AccountInfo accountInfo, String email, String ssn) {
        super(accountInfo);

        this.email = email;
        this.ssn = ssn;
    }

}
