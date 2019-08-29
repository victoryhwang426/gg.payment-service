package gg.paymentservice.store;

import gg.paymentservice.api.request.CreatePayment;
import gg.paymentservice.domain.Account;
import gg.paymentservice.domain.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Payments {

    private static final Map<String, Payment> PAYMENTS = new HashMap<>();

    public static ResponseEntity<Payment> create(CreatePayment request) {
        if(!Accounts.get(request.getSourceAccountId()).isPresent() ||
                !Accounts.get(request.getDestinationAccountId()).isPresent()){

            return ResponseEntity.notFound().build();
        }

        if(request.getSourceAccountId().equals(request.getDestinationAccountId()) ||
            request.getAmount() <= 0){

            return ResponseEntity.badRequest().build();
        }

        final String id = String.valueOf(System.nanoTime());
        final Payment payment = new Payment(
                id,
                request.getAmount(),
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                Payment.PaymentState.CREATED);

        PAYMENTS.put(id, payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    public static ResponseEntity<Payment> execute(String id){
        Optional<Payment> paymentOpt = get(id);
        if(!paymentOpt.isPresent()){
            return ResponseEntity.notFound().build();
        }

        Payment payment = paymentOpt.get();
        if(!payment.isStatusMovable()){
            return ResponseEntity.badRequest().build();
        }

        Optional<Account> sourceOpt = Accounts.get(payment.getSourceAccountId());
        Optional<Account> destinationOpt = Accounts.get(payment.getDestinationAccountId());
        if(!sourceOpt.isPresent() || !destinationOpt.isPresent()){
            return ResponseEntity.notFound().build();
        }

        Account sourceAccount = sourceOpt.get();
        Account destinationAccount = destinationOpt.get();

        if(sourceAccount.getBalance() < payment.getAmount()){
            payment.setPaymentState(Payment.PaymentState.REJECTED);
            return ResponseEntity.ok(payment);
        }

        sourceAccount.setBalance(sourceAccount.getBalance() - payment.getAmount());
        sourceAccount.addTransaction(Transactions.create(sourceAccount.getId(), payment.getAmount() * -1));

        destinationAccount.setBalance(destinationAccount.getBalance() + payment.getAmount());
        destinationAccount.addTransaction(Transactions.create(destinationAccount.getId(), payment.getAmount()));

        payment.setPaymentState(Payment.PaymentState.EXECUTED);
        return ResponseEntity.ok(payment);
    }

    public static ResponseEntity<Payment> cancel(String id){
        Optional<Payment> paymentOpt = get(id);
        if(!paymentOpt.isPresent()){
            return ResponseEntity.notFound().build();
        }

        Payment payment = paymentOpt.get();
        if(!payment.isStatusMovable()){
            return ResponseEntity.badRequest().build();
        }

        payment.setPaymentState(Payment.PaymentState.CANCELED);
        return ResponseEntity.ok(payment);
    }

    public static Optional<Payment> get(String id) {
        return Optional.ofNullable(PAYMENTS.get(id));
    }
}
