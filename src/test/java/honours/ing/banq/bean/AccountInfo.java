package honours.ing.banq.bean;

import honours.ing.banq.account.bean.NewAccountBean;

/**
 * @author jeffrey
 * @since 9-6-17
 */
public class AccountInfo {

    public String iBan;
    public String username;
    public String password;
    public String pin;
    public String cardNumber;
    public String token;

    public AccountInfo(NewAccountBean bean, String username, String password) {
        iBan = bean.getiBAN();
        pin = bean.getPinCode();
        cardNumber = bean.getPinCard();

        this.username = username;
        this.password = password;
    }

    public AccountInfo(AccountInfo accountInfo) {
        iBan = accountInfo.iBan;
        pin = accountInfo.pin;
        cardNumber = accountInfo.cardNumber;
        username = accountInfo.username;
        password = accountInfo.password;
    }

}
