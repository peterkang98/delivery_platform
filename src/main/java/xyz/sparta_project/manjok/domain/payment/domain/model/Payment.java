package xyz.sparta_project.manjok.domain.payment.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment (Aggregate Root)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class Payment {

    private String id;
    private String orderId;
    private String ordererId;
    private String tossPaymentKey;
    private String payToken;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime approvedAt;

    @Builder.Default
    private List<PaymentCancellation> cancellations = new ArrayList<>();

    // 감사 필드
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // 소프트 삭제
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private String deletedBy;

    /**
     * 팩토리 메서드 - 새로운 결제 생성
     */
    public static Payment create(
            String orderId,
            String ordererId,
            String tossPaymentKey,
            String payToken,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String createdBy,
            LocalDateTime createdAt
    ) {
        validatePaymentCreation(orderId, ordererId, tossPaymentKey, payToken, amount, paymentMethod);

        return Payment.builder()
                .orderId(orderId)
                .ordererId(ordererId)
                .tossPaymentKey(tossPaymentKey)
                .payToken(payToken)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .isDeleted(false)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(createdBy)
                .cancellations(new ArrayList<>())
                .build();
    }

    /**
     * 결제 생성 유효성 검증
     */
    private static void validatePaymentCreation(
            String orderId,
            String ordererId,
            String tossPaymentKey,
            String payToken,
            BigDecimal amount,
            PaymentMethod paymentMethod
    ) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDER_ID);
        }
        if (ordererId == null || ordererId.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_ORDERER_ID);
        }
        if (tossPaymentKey == null || tossPaymentKey.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_TOSS_PAYMENT_KEY);
        }
        if (payToken == null || payToken.trim().isEmpty()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAY_TOKEN);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.INVALID_AMOUNT);
        }
        if (paymentMethod == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
        }
    }

    /**
     * 결제 승인 처리
     */
    public void approve(LocalDateTime approvedAt, String approvedBy) {
        validatePendingStatus();
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
        updateAudit(approvedBy);
    }

    /**
     * 결제 실패 처리
     */
    public void fail(String failedBy) {
        validatePendingStatus();
        this.paymentStatus = PaymentStatus.FAILED;
        updateAudit(failedBy);
    }

    /**
     * 결제 취소 추가
     */
    public void addCancellation(
            CancellationType cancellationType,
            String reason,
            String requestedBy,
            BigDecimal cancelAmount,
            LocalDateTime cancelledAt
    ) {
        validateCancellable();
        validateCancelAmount(cancelAmount);
        validateCancelReason(reason);

        PaymentCancellation cancellation = PaymentCancellation.create(
                this.id,
                cancellationType,
                reason,
                requestedBy,
                cancelAmount,
                cancelledAt
        );

        this.cancellations.add(cancellation);

        // 전액 취소인 경우 상태 변경
        BigDecimal totalCancelledAmount = getTotalCancelledAmount();
        if (totalCancelledAmount.compareTo(this.amount) == 0) {
            this.paymentStatus = PaymentStatus.CANCELLED;
        } else {
            this.paymentStatus = PaymentStatus.PARTIALLY_CANCELLED;
        }

        updateAudit(requestedBy);
    }

    /**
     * 결제 금액 검증 (Order 도메인에서 호출)
     */
    public void validatePaymentAmount(BigDecimal orderAmount) {
        if (this.amount.compareTo(orderAmount) != 0) {
            throw new PaymentException(
                    PaymentErrorCode.AMOUNT_MISMATCH,
                    String.format("결제 금액(%s)과 주문 금액(%s)이 일치하지 않습니다.",
                            this.amount, orderAmount)
            );
        }
    }

    /**
     * 결제자 검증 (권한 체크용)
     */
    public boolean isPaidBy(String userId) {
        return this.ordererId.equals(userId);
    }

    /**
     * 소프트 삭제
     */
    public void softDelete(String deletedBy, LocalDateTime deletedAt) {
        if (this.paymentStatus != PaymentStatus.CANCELLED
                && this.paymentStatus != PaymentStatus.FAILED) {
            throw new PaymentException(
                    PaymentErrorCode.CANNOT_DELETE_PAYMENT,
                    "취소되거나 실패한 결제만 삭제할 수 있습니다."
            );
        }
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        updateAudit(deletedBy);
    }

    /**
     * 총 취소 금액 계산
     */
    public BigDecimal getTotalCancelledAmount() {
        return cancellations.stream()
                .map(PaymentCancellation::getCancelAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 남은 결제 금액 계산
     */
    public BigDecimal getRemainingAmount() {
        return amount.subtract(getTotalCancelledAmount());
    }

    /**
     * 취소 가능 여부 확인
     */
    public boolean isCancellable() {
        return this.paymentStatus == PaymentStatus.APPROVED
                || this.paymentStatus == PaymentStatus.PARTIALLY_CANCELLED;
    }

    /**
     * PENDING 상태 검증
     */
    private void validatePendingStatus() {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new PaymentException(
                    PaymentErrorCode.INVALID_PAYMENT_STATUS,
                    "결제 상태가 PENDING이 아닙니다. 현재 상태: " + this.paymentStatus
            );
        }
    }

    /**
     * 취소 가능 상태 검증
     */
    private void validateCancellable() {
        if (!isCancellable()) {
            throw new PaymentException(
                    PaymentErrorCode.CANNOT_CANCEL_PAYMENT,
                    "취소 가능한 결제 상태가 아닙니다. 현재 상태: " + this.paymentStatus
            );
        }
    }

    /**
     * 취소 금액 검증
     */
    private void validateCancelAmount(BigDecimal cancelAmount) {
        if (cancelAmount == null || cancelAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(
                    PaymentErrorCode.INVALID_CANCEL_AMOUNT,
                    "취소 금액은 0보다 커야 합니다."
            );
        }

        BigDecimal remainingAmount = getRemainingAmount();
        if (cancelAmount.compareTo(remainingAmount) > 0) {
            throw new PaymentException(
                    PaymentErrorCode.CANCEL_AMOUNT_EXCEEDS_REMAINING,
                    String.format("취소 금액(%s)이 남은 금액(%s)을 초과합니다.",
                            cancelAmount, remainingAmount)
            );
        }
    }

    /**
     * 취소 사유 검증
     */
    private void validateCancelReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new PaymentException(
                    PaymentErrorCode.CANCEL_REASON_REQUIRED,
                    "취소 사유는 필수입니다."
            );
        }
        if (reason.length() > 500) {
            throw new PaymentException(
                    PaymentErrorCode.CANCEL_REASON_TOO_LONG,
                    "취소 사유는 500자를 초과할 수 없습니다."
            );
        }
    }

    /**
     * 감사 필드 업데이트
     */
    private void updateAudit(String updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * cancellations의 불변성을 위한 방어적 복사
     */
    public List<PaymentCancellation> getCancellations() {
        return new ArrayList<>(cancellations);
    }
}