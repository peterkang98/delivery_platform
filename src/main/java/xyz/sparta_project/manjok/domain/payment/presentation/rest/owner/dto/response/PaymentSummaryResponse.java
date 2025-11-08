package xyz.sparta_project.manjok.domain.payment.presentation.rest.owner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentMethod;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 점주용 결제 요약 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private String paymentId;
    private String orderId;
    private String ordererId;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime approvedAt;

    public static PaymentSummaryResponse from(Payment payment) {
        return PaymentSummaryResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .ordererId(payment.getOrdererId())
                .amount(payment.getAmount())
                .remainingAmount(payment.getRemainingAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}