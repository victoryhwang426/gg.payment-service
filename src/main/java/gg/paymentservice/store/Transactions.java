package gg.paymentservice.store;

import gg.paymentservice.domain.Transaction;
import gg.paymentservice.statistics.Statistic;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

class Transactions {

    private static final Map<String, Transaction> TRANSACTIONS = new HashMap<>();

    static Transaction create(String account, Double amount) {
        final String id = String.valueOf(System.nanoTime());
        final Date created = Date.from(Instant.now());
        final Transaction transaction = new Transaction(id, account, amount, created);
        TRANSACTIONS.put(id, transaction);

        return transaction;
    }

    static List<Transaction> filterTransactions(List<Transaction> transactions,
                                                Date from){
        return transactions.stream()
                .filter(t -> between(t.getCreated(), from))
                .filter(t -> t.getAmount() > 0)
                .collect(Collectors.toList());
    }

    static boolean between(Date target, Date from){
        if (from == null) {
            return true;
        }

        return from.toInstant().compareTo(target.toInstant()) <= 0;
    }

    static Statistic setStatistic(String accountId, List<Transaction> transactions){
        Double maxTrans = transactions.stream()
                .max(Comparator.comparing(Transaction::getAmount))
                .map(t -> t.getAmount())
                .get();
        Double minTrans = transactions.stream()
                .min(Comparator.comparing(Transaction::getAmount))
                .map(t -> t.getAmount())
                .get();
        Double avgTrans = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .getAsDouble();

        return new Statistic(
                accountId,
                maxTrans,
                minTrans,
                avgTrans,
                transactions
        );
    }
}
