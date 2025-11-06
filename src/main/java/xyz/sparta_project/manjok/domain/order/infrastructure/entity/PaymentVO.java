package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Payment;

/**
 * Payment Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentVO {

    @Column(name = "payment_id", nullable = false, length = 100)
    private String paymentId;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    /**
     * 도메인 → VO
     */
    public static PaymentVO from(Payment domain) {
        return PaymentVO.builder()
                .paymentId(domain.getPaymentId())
                .isPaid(domain.getIsPaid())
                .build();
    }

    /**
     * VO → 도메인
     */
    public Payment toDomain() {
        return Payment.builder()
                .paymentId(this.paymentId)
                .isPaid(this.isPaid)
                .build();
    }
}