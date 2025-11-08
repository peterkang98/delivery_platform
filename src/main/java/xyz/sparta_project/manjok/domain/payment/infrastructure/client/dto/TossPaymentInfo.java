package xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto;

import java.math.BigDecimal;

public record TossPaymentInfo(
        String orderId,
        String paymentKey,
        BigDecimal totalAmount,
        String secret
) {}
