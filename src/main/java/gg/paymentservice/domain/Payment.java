package gg.paymentservice.domain;

public class Payment {
    public enum PaymentState {
        CREATED, EXECUTED, REJECTED, CANCELED
    }

    private String id;
    private Double amount;
    private String sourceAccountId;
    private String destinationAccountId;
    private String state;

    public Payment(String id,
                   Double amount,
                   String sourceAccountId,
                   String destinationAccountId,
                   PaymentState paymentState) {
        this.id = id;
        this.amount = amount;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.state = paymentState.name();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public String getState() {
        return state;
    }

    public void setPaymentState(PaymentState paymentState) {
        this.state = paymentState.name();
    }

    public boolean isStatusMovable(){
        return this.state.equals(PaymentState.CREATED.name());
    }

    @Override
    public String toString() {
        return id;
    }
}
