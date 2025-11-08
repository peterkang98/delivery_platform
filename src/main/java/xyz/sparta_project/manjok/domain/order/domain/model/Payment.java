package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

/**
 * Payment (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"paymentId"})
public class Payment {

    private String paymentId;
    private Boolean isPaid;

    /**
     * 팩토리 메서드 - 결제 정보 생성
     */
    public static Payment create(String paymentId, Boolean isPaid) {
        validatePayment(paymentId, isPaid);

        return Payment.builder()
                .paymentId(paymentId)
                .isPaid(isPaid)
                .build();
    }

    /**
     * 팩토리 메서드 - 미결제 상태로 생성
     */
    public static Payment createPending(String paymentId) {
        return create(paymentId, false);
    }

    /**
     * 결제 정보 유효성 검증
     */
    private static void validatePayment(String paymentId, Boolean isPaid) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_PAYMENT, "결제 ID는 필수입니다.");
        }
        if (isPaid == null) {
            throw new OrderException(OrderErrorCode.INVALID_PAYMENT, "결제 상태는 필수입니다.");
        }
    }

    /**
     * 결제 완료 여부 확인
     */
    public boolean isCompleted() {
        return Boolean.TRUE.equals(isPaid);
    }
}