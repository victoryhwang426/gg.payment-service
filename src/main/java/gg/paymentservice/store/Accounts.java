package gg.paymentservice.store;

import gg.paymentservice.api.request.CreateAccount;
import gg.paymentservice.domain.Account;
import gg.paymentservice.domain.Transaction;
import gg.paymentservice.statistics.Statistics;

import java.time.Instant;
import java.util.*;

public class Accounts {

    private static final Map<String, Account> ACCOUNTS = new HashMap<>();

    public static String create(CreateAccount request) {
        final String id = String.valueOf(System.nanoTime());
        final Account account = new Account(id);
        account.setBalance(request.getBalance());
        ACCOUNTS.put(id, account);
        return id;
    }

    public static void transaction(String id, Double amount) {
        final Optional<Account> accountOpt = get(id);
        if (!accountOpt.isPresent()) {
            return;
        }

        Account account = accountOpt.get();
        account.addTransaction(Transactions.create(id, amount));
        account.updateBalance(amount);
        ACCOUNTS.put(id, account);
    }

    public static Optional<Account> get(String id) {
        return Optional.ofNullable(ACCOUNTS.get(id));
    }

    public static List<Account> all() {
        return new ArrayList<>(ACCOUNTS.values());
    }

    public static Statistics getStatistics(Integer periodInSeconds){
        Statistics statistics = new Statistics();

        if(periodInSeconds > 10){
            return statistics;
        }

        List<Account> accounts = Accounts.all();
        Date from = Date.from(Instant.now().minusSeconds(periodInSeconds));
        for(Account account : accounts){
            List<Transaction> transactions =
                Transactions.filterTransactions(account.getTransactions(), from);

            if(!transactions.isEmpty()){
                statistics.add( Transactions.setStatistic(account.getId(), transactions) );
            }
        }

        return statistics;
    }
}
