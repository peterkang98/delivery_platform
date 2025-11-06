package xyz.sparta_project.manjok.domain.payment.presentation.rest.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import xyz.sparta_project.manjok.domain.payment.domain.model.CancellationType;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentCancellation;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentMethod;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 결제 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailResponse {

    private String paymentId;
    private String orderId;
    private String tossPaymentKey;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    // 취소 정보
    private BigDecimal totalCancelledAmount;
    private BigDecimal remainingAmount;
    private List<CancellationInfo> cancellations;

    /**
     * Domain → DTO
     */
    public static PaymentDetailResponse from(Payment payment) {
        return PaymentDetailResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .tossPaymentKey(payment.getTossPaymentKey())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .approvedAt(payment.getApprovedAt())
                .createdAt(payment.getCreatedAt())
                .totalCancelledAmount(payment.getTotalCancelledAmount())
                .remainingAmount(payment.getRemainingAmount())
                .cancellations(payment.getCancellations().stream()
                        .map(CancellationInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancellationInfo {
        private String cancellationId;
        private CancellationType cancellationType;
        private String reason;
        private BigDecimal cancelAmount;
        private LocalDateTime cancelledAt;

        public static CancellationInfo from(PaymentCancellation cancellation) {
            return CancellationInfo.builder()
                    .cancellationId(cancellation.getId())
                    .cancellationType(cancellation.getCancellationType())
                    .reason(cancellation.getReason())
                    .cancelAmount(cancellation.getCancelAmount())
                    .cancelledAt(cancellation.getCancelledAt())
                    .build();
        }
    }
}