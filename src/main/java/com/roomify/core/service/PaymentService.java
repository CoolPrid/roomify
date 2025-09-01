package com.roomify.core.service;

import com.roomify.core.dto.PaymentResult;

public interface PaymentService {
    PaymentResult charge(String userId, double amount);
}
