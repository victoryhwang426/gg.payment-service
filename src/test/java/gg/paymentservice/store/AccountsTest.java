package gg.paymentservice.store;

import gg.paymentservice.api.request.CreateAccount;
import gg.paymentservice.domain.Account;
import gg.paymentservice.statistics.Statistics;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class AccountsTest {
    private String ACCOUNT_ID;
    private CreateAccount CREATE_ACCOUNT = new CreateAccount(5000d);

    @Before
    public void setup(){
        this.ACCOUNT_ID = Accounts.create(CREATE_ACCOUNT);
    }

    @Test
    public void getStatistics_should_return_statistics(){
        Integer periodInSeconds = 10;

        String id = Accounts.create(CREATE_ACCOUNT);

        Accounts.transaction(id,5000d);
        Accounts.transaction(id,4000d);
        Accounts.transaction(id,3000d);
        Accounts.transaction(id,2000d);
        Accounts.transaction(id,1000d);

        Statistics statistics = Accounts.getStatistics(periodInSeconds);

        assertTrue(statistics.getStatistics().containsKey(id));
        assertEquals(5000d, statistics.getStatistics().get(id).getMaxTrans(), 0d);
        assertEquals(1000d, statistics.getStatistics().get(id).getMinTrans(), 0d);
        assertEquals(3000d, statistics.getStatistics().get(id).getAvgTrans(), 0d);
        assertEquals(5, statistics.getStatistics().get(id).getTransactions().size());
    }

    @Test
    public void getStatistics_should_return_empty_statistics_when_amount_in_all_transactions_are_less_0(){
        Integer periodInSeconds = 10;

        String id = Accounts.create(CREATE_ACCOUNT);

        Accounts.transaction(id,-5000d);
        Accounts.transaction(id,-4000d);
        Accounts.transaction(id,-3000d);
        Accounts.transaction(id,-2000d);
        Accounts.transaction(id,-1000d);

        Statistics statistics = Accounts.getStatistics(periodInSeconds);

        assertFalse(statistics.getStatistics().containsKey(id));
    }

    @Test
    public void getStatistics_should_return_empty_statistics_when_periodInSeconds_is_bigger_then_10(){
        Integer periodInSeconds = 11;

        Statistics statistics = Accounts.getStatistics(periodInSeconds);

        assertTrue(statistics.getStatistics().isEmpty());
    }

    @Test
    public void create_should_return_id_when_it_is_stored_in_map(){
        String accountId = Accounts.create(CREATE_ACCOUNT);
        Optional<Account> optionalAccount = Accounts.get(accountId);

        assertTrue(optionalAccount.isPresent());
        assertEquals(accountId, optionalAccount.get().getId());
        assertEquals(5000d, optionalAccount.get().getBalance(), 0d);
    }

    @Test
    public void transaction_should_add_transaction_log(){
        Accounts.transaction(ACCOUNT_ID, 1000d);

        Optional<Account> optionalAccount = Accounts.get(ACCOUNT_ID);
        Account account = optionalAccount.get();

        assertEquals(1, account.getTransactions().size());
        assertEquals(1000d, account.getTransactions().get(0).getAmount(), 0d);
    }

    @Test
    public void transaction_should_update_account_balance(){
        Accounts.transaction(ACCOUNT_ID, 1000d);

        Optional<Account> optionalAccount = Accounts.get(ACCOUNT_ID);
        Account account = optionalAccount.get();

        assertEquals(6000d, account.getBalance(), 0d);
    }

    @Test
    public void transaction_should_not_add_and_update_when_account_is_not_found(){
        Accounts.transaction("A1", 1000d);

        Optional<Account> optionalAccount = Accounts.get("A1");

        assertFalse(optionalAccount.isPresent());
    }

    @Test
    public void get_should_return_correct_account_as_optional(){
        Optional<Account> optionalAccount = Accounts.get(ACCOUNT_ID);

        assertTrue(optionalAccount.isPresent());
        assertEquals(ACCOUNT_ID, optionalAccount.get().getId());
    }

    @Test
    public void all_should_return_all_saved_accounts(){
        List<Account> accounts = Accounts.all();

        assertTrue(accounts.size() > 0);
    }
}