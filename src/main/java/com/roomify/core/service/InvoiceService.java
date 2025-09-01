package com.roomify.core.service;

public class InvoiceService {
    public String generateInvoiceId() {
        return java.util.UUID.randomUUID().toString();
    }
}
