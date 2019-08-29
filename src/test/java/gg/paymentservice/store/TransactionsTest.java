package gg.paymentservice.store;

import gg.paymentservice.domain.Transaction;
import gg.paymentservice.statistics.Statistic;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TransactionsTest {
    @Test
    public void create_should_return_transaction_when_it_is_stored_in_map(){
        Date threshold = new Date();
        Transaction transaction = Transactions.create("A1", 5000d);

        assertEquals("A1", transaction.getAccount());
        assertNotNull(transaction.getId());
        assertTrue(transaction.getCreated().getTime() >= threshold.getTime());
    }

    @Test
    public void between_should_return_true_when_from_is_null(){
        boolean result = Transactions.between(new Date(), null);

        assertTrue(result);
    }

    @Test
    public void between_should_return_true_when_target_date_is_between_from_and_to(){
        int periodInSeconds = 5;
        LocalDateTime targetTime = LocalDateTime.of(2019, 8, 29, 10, 0, 50,0);
        LocalDateTime currentTime = LocalDateTime.of(2019, 8, 29, 10, 0, 55,0);

        Date target = Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant());
        Date from = Date.from(currentTime.minusSeconds(periodInSeconds).atZone(ZoneId.systemDefault()).toInstant());

        boolean result = Transactions.between(target, from);

        assertTrue(result);
    }

    @Test
    public void between_should_return_false_when_target_date_is_not_between_from_and_to(){
        int periodInSeconds = 5;
        LocalDateTime targetTime = LocalDateTime.of(2019, 8, 29, 10, 0, 50,0);
        LocalDateTime currentTime = LocalDateTime.of(2019, 8, 29, 10, 0, 59,0);

        Date target = Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant());
        Date from = Date.from(currentTime.minusSeconds(periodInSeconds).atZone(ZoneId.systemDefault()).toInstant());

        boolean result = Transactions.between(target, from);

        assertFalse(result);
    }

    @Test
    public void filterTransactions_should_return_transaction_as_a_amount_is_bigger_then_0(){
        List<Transaction> tempTransactions = Arrays.asList(
            new Transaction(String.valueOf(System.nanoTime()), "A1", 10d, new Date()),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 20d, new Date()),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 30d, new Date()),
            new Transaction(String.valueOf(System.nanoTime()), "A1", -10d, new Date())
        );

        List<Transaction> transactions =
            Transactions.filterTransactions(tempTransactions, null);

        assertEquals(3, transactions.size());
    }

    @Test
    public void filterTransactions_should_return_transaction_as_a_created_is_bigger_then_threshold(){
        Instant now = Instant.now();

        List<Transaction> tempTransactions = Arrays.asList(
            new Transaction(String.valueOf(System.nanoTime()), "A1", 10d, Date.from(now.minusSeconds(10))),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 20d, Date.from(now.minusSeconds(20))),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 30d, Date.from(now.minusSeconds(30)))
        );

        Integer periodSeconds = 2;
        List<Transaction> transactions =
                Transactions.filterTransactions(tempTransactions, Date.from(Instant.now().minusSeconds(periodSeconds)));

        assertEquals(0, transactions.size());
    }

    @Test
    public void setStatistic_should_return_Statistic(){
        List<Transaction> transactions = Arrays.asList(
            new Transaction(String.valueOf(System.nanoTime()), "A1", 10d, new Date()),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 20d, new Date()),
            new Transaction(String.valueOf(System.nanoTime()), "A1", 30d, new Date())
        );

        Statistic statistic = Transactions.setStatistic("A1", transactions);

        assertEquals("A1", statistic.getAccountId());
        assertEquals(30d, statistic.getMaxTrans(), 0d);
        assertEquals(10d, statistic.getMinTrans(), 0d);
        assertEquals(20d, statistic.getAvgTrans(), 0d);
        assertEquals(3, statistic.getTransactions().size());
    }
}