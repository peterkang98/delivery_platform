package xyz.sparta_project.manjok.domain.payment.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import xyz.sparta_project.manjok.domain.payment.domain.model.CancellationType;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentCancellation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentCancellation Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentCancellationVO {

    @Column(name = "cancellation_id", nullable = false, length = 50)
    private String cancellationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_type", nullable = false, length = 30)
    private CancellationType cancellationType;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "requested_by", nullable = false, length = 36)
    private String requestedBy;

    @Column(name = "cancel_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal cancelAmount;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    /**
     * 도메인 → VO
     */
    public static PaymentCancellationVO from(PaymentCancellation domain) {
        return PaymentCancellationVO.builder()
                .cancellationId(domain.getId())
                .cancellationType(domain.getCancellationType())
                .reason(domain.getReason())
                .requestedBy(domain.getRequestedBy())
                .cancelAmount(domain.getCancelAmount())
                .cancelledAt(domain.getCancelledAt())
                .build();
    }

    /**
     * VO → 도메인
     */
    public PaymentCancellation toDomain() {
        return PaymentCancellation.builder()
                .id(this.cancellationId)
                .paymentId(null)  // VO에서는 paymentId를 관리하지 않음
                .cancellationType(this.cancellationType)
                .reason(this.reason)
                .requestedBy(this.requestedBy)
                .cancelAmount(this.cancelAmount)
                .cancelledAt(this.cancelledAt)
                .build();
    }
}