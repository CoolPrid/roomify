package com.roomify.core.dto;

public class PaymentResult {
    private final boolean success;
    private final String transactionId;

    public PaymentResult(boolean success, String transactionId) {
        this.success = success;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
}
