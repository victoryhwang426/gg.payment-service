package gg.paymentservice.util;

import gg.paymentservice.api.request.CreateAccount;
import gg.paymentservice.domain.Account;
import gg.paymentservice.domain.Transaction;
import gg.paymentservice.statistics.Statistic;
import gg.paymentservice.statistics.Statistics;
import gg.paymentservice.store.Accounts;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TransactionsGenerator {
    private static final int ACCOUNTS = 100;
    private static final int DURATION = 10;
    private static final double MIN_AMOUNT = 10d;
    private static final double MAX_AMOUNT = 1000d;
    private static final double BALANCE = 1000000d;

    private List<String> genAccounts = new ArrayList<>();

    public void generate() throws InterruptedException {
        generateAccounts();

        long start = System.currentTimeMillis();
        long later = start + TimeUnit.SECONDS.toMillis(DURATION);
        while (start < later) {
            start = System.currentTimeMillis();
            Thread.sleep(10);

            final RandomAccounts randomAccounts = randomAccounts();
            final double amount = randomAmount();
            Accounts.transaction(randomAccounts.a1, -amount);
            Accounts.transaction(randomAccounts.a2, amount);
        }
    }

    public Statistics calculate(int periodInSeconds) {
        Date when = Date.from(Instant.now().minusSeconds(periodInSeconds));
        final Statistics statistics = new Statistics();
        Accounts.all()
                .forEach(account -> calculate(periodInSeconds, when, statistics, account));

        return statistics;
    }

    private void calculate(int periodInSeconds, Date when, Statistics statistics, Account account) {
        Double max = -1d;
        Double min = MAX_AMOUNT + 1;
        Double sum = 0d;
        List<Transaction> filteredTransactions = account.getTransactions()
                .stream()
                .filter(transaction -> within(transaction.getCreated(), when, periodInSeconds))
                .filter(transaction -> transaction.getAmount() > 0)
                .collect(Collectors.toList());

        if (filteredTransactions.isEmpty()) {
            return;
        }

        for (Transaction t : filteredTransactions) {
            max = max > t.getAmount() ? max : t.getAmount();
            min = min < t.getAmount() ? min : t.getAmount();
            sum += t.getAmount();
        }

        statistics.add(new Statistic(account.getId(), max == -1d ? 0 : max, min == MAX_AMOUNT + 1 ? 0 : min,
                sum / filteredTransactions.size(),
                filteredTransactions));
    }

    private void generateAccounts() {
        for (int i = 0; i < ACCOUNTS; i++) {
            genAccounts.add(Accounts.create(new CreateAccount(BALANCE)));
        }
    }

    private double randomAmount() {
        return MathUtils.round(RandomUtils.nextDouble(MIN_AMOUNT, MAX_AMOUNT));
    }

    private RandomAccounts randomAccounts() {
        final int size = genAccounts.size();
        final String a1 = genAccounts.get(RandomUtils.nextInt(0, size));
        String a2 = genAccounts.get(RandomUtils.nextInt(0, size));
        while (a1.equals(a2)) {
            a2 = genAccounts.get(RandomUtils.nextInt(0, size));
        }

        return new RandomAccounts(a1, a2);
    }

    private class RandomAccounts {
        String a1;
        String a2;

        RandomAccounts(String a1, String a2) {
            this.a1 = a1;
            this.a2 = a2;
        }
    }

    private boolean within(Date d1, Date d2, int seconds) {
        if (d2 == null) {
            return true;
        }

        final long diff = d2.toInstant().getEpochSecond() - d1.toInstant().getEpochSecond();
        return diff >= 0 && diff < seconds;
    }
}
