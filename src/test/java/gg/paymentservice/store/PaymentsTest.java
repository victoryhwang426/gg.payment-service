package gg.paymentservice.store;

import gg.paymentservice.api.request.CreateAccount;
import gg.paymentservice.api.request.CreatePayment;
import gg.paymentservice.domain.Account;
import gg.paymentservice.domain.Payment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.Assert.*;

public class PaymentsTest {
    private String SOURCE_ACCOUNT_ID;
    private String DESTINATION_ID;
    private CreatePayment CREATE_PAYMENT;

    @Before
    public void setup(){
        this.SOURCE_ACCOUNT_ID = Accounts.create(new CreateAccount(5000d));
        this.DESTINATION_ID = Accounts.create(new CreateAccount(5000d));
        this.CREATE_PAYMENT = new CreatePayment(1000d, SOURCE_ACCOUNT_ID, DESTINATION_ID);
    }

    @Test
    public void create_should_return_not_found_when_account_is_not_found(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.create(
            new CreatePayment(5000d, "A1", "A2")
        );

        assertEquals(HttpStatus.NOT_FOUND, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void create_should_return_bad_request_when_accounts_are_same(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.create(
            new CreatePayment(5000d, SOURCE_ACCOUNT_ID, SOURCE_ACCOUNT_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void create_should_return_bad_request_when_amount_is_less_then_0(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.create(
            new CreatePayment(0d, SOURCE_ACCOUNT_ID, DESTINATION_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void create_should_return_created_when_it_is_stored_with_CREATED_as_status_in_map(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.create(CREATE_PAYMENT);

        assertEquals(HttpStatus.CREATED, paymentResponseEntity.getStatusCode());
        assertEquals(Payment.PaymentState.CREATED.name(), paymentResponseEntity.getBody().getState());
    }

    @Test
    public void get_should_return_payment_as_optional(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.create(CREATE_PAYMENT);
        String paymentId = paymentResponseEntity.getBody().getId();

        Optional<Payment> optionalPayment = Payments.get(paymentId);

        assertTrue(optionalPayment.isPresent());
    }

    @Test
    public void cancel_should_return_not_found_when_paymentId_is_invalid(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.cancel("Test");

        assertEquals(HttpStatus.NOT_FOUND, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void cancel_should_return_bad_request_when_payment_is_not_movable(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();
        tempPayment.setPaymentState(Payment.PaymentState.EXECUTED);

        ResponseEntity<Payment> paymentResponseEntity = Payments.cancel(tempPayment.getId());

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void cancel_should_return_ok_when_status_is_changed_as_CANCELD(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();

        ResponseEntity<Payment> paymentResponseEntity = Payments.cancel(tempPayment.getId());
        Payment payment = paymentResponseEntity.getBody();

        assertEquals(HttpStatus.OK, paymentResponseEntity.getStatusCode());
        assertEquals(Payment.PaymentState.CANCELED.name(), payment.getState());
    }

    @Test
    public void execute_should_return_not_found_when_payment_is_not_invalid(){
        ResponseEntity<Payment> paymentResponseEntity = Payments.execute("Test");

        assertEquals(HttpStatus.NOT_FOUND, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void execute_should_return_bad_request_when_payment_is_not_movable(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();
        tempPayment.setPaymentState(Payment.PaymentState.CANCELED);

        ResponseEntity<Payment> paymentResponseEntity = Payments.execute(tempPayment.getId());

        assertEquals(HttpStatus.BAD_REQUEST, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void execute_should_return_not_found_when_account_is_not_found(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();
        tempPayment.setSourceAccountId("A1");

        ResponseEntity<Payment> paymentResponseEntity = Payments.execute(tempPayment.getId());

        assertEquals(HttpStatus.NOT_FOUND, paymentResponseEntity.getStatusCode());
    }

    @Test
    public void execute_should_return_status_as_REJECTED_when_balance_from_source_is_less_then_payment_amount(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();
        tempPayment.setAmount(6000d);

        ResponseEntity<Payment> paymentResponseEntity = Payments.execute(tempPayment.getId());
        Payment payment = paymentResponseEntity.getBody();

        assertEquals(HttpStatus.OK, paymentResponseEntity.getStatusCode());
        assertEquals(Payment.PaymentState.REJECTED.name(), payment.getState());
    }

    @Test
    public void execute_should_return_ok_when_status_is_changed_as_EXECUTED(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();

        ResponseEntity<Payment> paymentResponseEntity = Payments.execute(tempPayment.getId());
        Payment payment = paymentResponseEntity.getBody();

        assertEquals(HttpStatus.OK, paymentResponseEntity.getStatusCode());
        assertEquals(Payment.PaymentState.EXECUTED.name(), payment.getState());
    }

    @Test
    public void execute_should_add_transaction_each_accounts_when_status_is_changed_as_EXECUTED(){
        ResponseEntity<Payment> tempEntity = Payments.create(CREATE_PAYMENT);
        Payment tempPayment = tempEntity.getBody();

        ResponseEntity<Payment> paymentResponseEntity = Payments.execute(tempPayment.getId());
        Payment payment = paymentResponseEntity.getBody();

        Optional<Account> optionalSourceAccount = Accounts.get(payment.getSourceAccountId());
        Optional<Account> optionalDestinationAccount = Accounts.get(payment.getDestinationAccountId());

        assertEquals(Payment.PaymentState.EXECUTED.name(), payment.getState());
        assertTrue(optionalSourceAccount.get().getTransactions().size() == 1);
        assertTrue(optionalDestinationAccount.get().getTransactions().size() == 1);
    }
}