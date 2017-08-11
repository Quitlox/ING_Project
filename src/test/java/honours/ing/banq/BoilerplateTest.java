package honours.ing.banq;

import honours.ing.banq.account.BankAccountService;
import honours.ing.banq.auth.AuthService;
import honours.ing.banq.bean.AccountInfo;
import honours.ing.banq.card.CardService;
import honours.ing.banq.config.TestConfiguration;
import honours.ing.banq.info.InfoService;
import honours.ing.banq.time.TimeService;
import honours.ing.banq.transaction.TransactionService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Kevin Witlox
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
@Import(TestConfiguration.class)
@ActiveProfiles("test")
public class BoilerplateTest {

    // Services
    @Autowired
    protected CardService cardService;

    @Autowired
    protected BankAccountService bankAccountService;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected InfoService infoService;

    @Autowired
    protected TimeService timeService;

    // Fields
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    protected TestAccountInfo account1, account2;

    @Before
    public void setUp() throws Exception {
        account1 = new TestAccountInfo(new AccountInfo(bankAccountService.openAccount("Jan", "Jansen", "J.", "1996-1-1",
                                                                                      "1234567890", "Klaverstraat 1",
                                                                                      "0612345678",
                                                                                      "janjansen@gmail.com", "jantje96",
                                                                                      "1234"), "jantje96", "1234"),
                                       "janjansen@gmail.com", "1234567890");
        account2 = new TestAccountInfo(
                new AccountInfo(bankAccountService.openAccount("Piet", "Pietersen", "p.p", "1998-8-8",
                                                               "012345789", "Huisstraat 1", "0607080910",
                                                               "piet@gmail.com", "piet1", "1234"), "piet1", "1234"),
                "piet@gmail.com", "012345789");

        account1.token = authService.getAuthToken("jantje96", "1234").getAuthToken();
        account2.token = authService.getAuthToken("piet1", "1234").getAuthToken();

        assertThat(infoService.getBalance(account1.token, account1.iBan).getBalance(), equalTo
                (0d));
        assertThat(infoService.getBalance(account2.token, account2.iBan).getBalance(), equalTo
                (0d));

    }

}
