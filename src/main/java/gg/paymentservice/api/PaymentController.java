package gg.paymentservice.api;

import gg.paymentservice.api.request.CreatePayment;
import gg.paymentservice.domain.Payment;
import gg.paymentservice.store.Payments;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment")
    public class PaymentController {

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody CreatePayment request) {
        return Payments.create(request);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Payment> get(@PathVariable("id") String id) {
        final Optional<Payment> paymentOpt = Payments.get(id);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.notFound()
                    .build();
        }

        return ResponseEntity.ok(paymentOpt.get());
    }

    @PutMapping(path = "/execute/{id}")
    public ResponseEntity<Payment> execute(@PathVariable("id") String id) {
        return Payments.execute(id);
    }

    @PutMapping(path = "/cancel/{id}")
    public ResponseEntity<Payment> cancel(@PathVariable("id") String id) {
        return Payments.cancel(id);
    }
}
