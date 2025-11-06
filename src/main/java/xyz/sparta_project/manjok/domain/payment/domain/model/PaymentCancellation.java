package xyz.sparta_project.manjok.domain.payment.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentCancellation (Entity)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class PaymentCancellation {

    private String id;
    private String paymentId;
    private CancellationType cancellationType;
    private String reason;
    private String requestedBy;
    private BigDecimal cancelAmount;
    private LocalDateTime cancelledAt;

    /**
     * 팩토리 메서드 - 결제 취소 생성
     */
    public static PaymentCancellation create(
            String paymentId,
            CancellationType cancellationType,
            String reason,
            String requestedBy,
            BigDecimal cancelAmount,
            LocalDateTime cancelledAt
    ) {
        validateCancellation(paymentId, cancellationType, reason, requestedBy, cancelAmount);

        return PaymentCancellation.builder()
                .id(generateCancellationId())
                .paymentId(paymentId)
                .cancellationType(cancellationType)
                .reason(reason)
                .requestedBy(requestedBy)
                .cancelAmount(cancelAmount)
                .cancelledAt(cancelledAt)
                .build();
    }

    /**
     * 취소 ID 생성
     */
    private static String generateCancellationId() {
        return "PC-" + UUID.randomUUID().toString();
    }

    /**
     * 취소 정보 유효성 검증
     */
    private static void validateCancellation(
            String paymentId,
            CancellationType cancellationType,
            String reason,
            String requestedBy,
            BigDecimal cancelAmount
    ) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_ID);
        }
        if (cancellationType == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_CANCELLATION_TYPE);
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.CANCEL_REASON_REQUIRED);
        }
        if (requestedBy == null || requestedBy.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_REQUESTER);
        }
        if (cancelAmount == null || cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_CANCEL_AMOUNT);
        }
    }

    /**
     * 사용자에 의한 취소인지 확인
     */
    public boolean isUserCancellation() {
        return this.cancellationType == CancellationType.USER_REQUEST;
    }

    /**
     * 시스템에 의한 취소인지 확인
     */
    public boolean isSystemCancellation() {
        return this.cancellationType == CancellationType.SYSTEM_ERROR;
    }

    /**
     * 관리자에 의한 취소인지 확인
     */
    public boolean isAdminCancellation() {
        return this.cancellationType == CancellationType.ADMIN_CANCEL;
    }
}