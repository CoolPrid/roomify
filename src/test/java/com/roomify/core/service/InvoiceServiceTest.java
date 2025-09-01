package com.roomify.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceServiceTest {

    private final InvoiceService invoiceService = new InvoiceService();

    @Test
    void generateInvoiceId_notNull_andUnique() {
        String a = invoiceService.generateInvoiceId();
        String b = invoiceService.generateInvoiceId();
        assertNotNull(a);
        assertNotNull(b);
        assertNotEquals(a, b);
    }
}
