package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

/**
 * OrderStatus (Enum)
 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PAYMENT_PENDING("결제 대기", null),  // 초기 상태
    PAYMENT_COMPLETED("결제 완료", PAYMENT_PENDING),
    PENDING("주문 대기", PAYMENT_COMPLETED),
    CONFIRMED("가게 확인", PENDING),
    PREPARING("조리 중", CONFIRMED),
    DELIVERING("배달 중", PREPARING),
    COMPLETED("완료", DELIVERING),
    CANCELED("취소", null);  // 특별 처리 필요

    private final String description;
    private final OrderStatus previousStatus;

    /**
     * 현재 상태에서 다음 상태로 전환 가능한지 검증
     */
    public void validateTransition(OrderStatus nextStatus) {
        // CANCELED는 특별 처리
        if (nextStatus == CANCELED) {
            if (!this.isCancelable()) {
                throw new OrderException(
                        OrderErrorCode.INVALID_STATUS_TRANSITION,
                        String.format("'%s' 상태에서는 취소할 수 없습니다.", this.description)
                );
            }
            return;
        }

        // 일반적인 순차 전환
//        if (nextStatus.previousStatus != this) {
//            throw new OrderException(
//                    OrderErrorCode.INVALID_STATUS_TRANSITION,
//                    String.format("'%s' 상태에서 '%s' 상태로 전환할 수 없습니다.",
//                            this.description, nextStatus.description)
//            );
//        }
    }

    /**
     * 결제 완료 상태인지 확인
     */
    public boolean isPaymentCompleted() {
        return this == PAYMENT_COMPLETED
                || this == PENDING
                || this == CONFIRMED
                || this == PREPARING
                || this == DELIVERING
                || this == COMPLETED;
    }

    /**
     * 취소 가능한 상태인지 확인
     */
    public boolean isCancelable() {
        return this == PAYMENT_PENDING
                || this == PAYMENT_COMPLETED
                || this == PENDING
                || this == CONFIRMED
                || this == PREPARING;
    }

    /**
     * 진행 중인 상태인지 확인 (완료되지 않고 취소되지 않은 상태)
     */
    public boolean isInProgress() {
        return this != COMPLETED && this != CANCELED;
    }

    /**
     * 최종 상태인지 확인 (더 이상 상태 변경이 없는 상태)
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELED;
    }

    /**
     * 가게에서 처리 가능한 상태인지 확인
     */
    public boolean isRestaurantProcessable() {
        return this == PENDING
                || this == CONFIRMED
                || this == PREPARING;
    }
}