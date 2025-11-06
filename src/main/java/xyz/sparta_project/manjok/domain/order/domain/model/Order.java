package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order (Aggregate Root)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class Order {

    private static final long CANCEL_AVAILABLE_MINUTES = 5;

    private String id;
    private Orderer orderer;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private OrderStatus status;
    private Payment payment;
    private BigDecimal totalPrice;

    private LocalDateTime requestedAt;
    private LocalDateTime paymentCompletedAt;  // 결제 완료 시간 추가
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
    private String cancelReason;

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
     * 팩토리 메서드 - 새로운 주문 생성
     */
    public static Order create(Orderer orderer, List<OrderItem> items,
                               Payment payment, LocalDateTime requestedAt,
                               String createdBy) {
        validateOrderCreation(orderer, items, payment);

        List<OrderItem> itemsCopy = new ArrayList<>(items);
        BigDecimal calculatedTotalPrice = calculateTotalPriceFromItems(itemsCopy);

        return Order.builder()
                .orderer(orderer)
                .items(itemsCopy)
                .payment(payment)
                .status(OrderStatus.PAYMENT_PENDING)
                .requestedAt(requestedAt)
                .totalPrice(calculatedTotalPrice)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .isDeleted(false)
                .build();
    }

    /**
     * 주문 생성 유효성 검증
     */
    private static void validateOrderCreation(Orderer orderer, List<OrderItem> items, Payment payment) {
        if (orderer == null) {
            throw new OrderException(OrderErrorCode.INVALID_ORDERER);
        }
        if (items == null || items.isEmpty()) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_ITEMS);
        }
        if (payment == null) {
            throw new OrderException(OrderErrorCode.INVALID_PAYMENT);
        }
    }

    /**
     * 전체 주문 가격 계산
     */
    private static BigDecimal calculateTotalPriceFromItems(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 결제 완료 처리
     * @param paymentId Payment 도메인의 실제 엔티티 ID
     * @param paymentCompletedAt 결제 완료 시간
     * @param updatedBy 수정자
     */
    public void completePayment(String paymentId, LocalDateTime paymentCompletedAt, String updatedBy) {
        validateStatusTransition(OrderStatus.PAYMENT_COMPLETED);

        // Payment 도메인의 실제 ID로 Payment 객체 업데이트 (isPaid = true)
        this.payment = Payment.create(paymentId, true);
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentCompletedAt = paymentCompletedAt;

        updateAudit(updatedBy);
    }

    /**
     * 주문 대기 상태로 전환
     */
    public void toPending(String updatedBy) {
        if (this.status != OrderStatus.PAYMENT_COMPLETED) {
            throw new OrderException(OrderErrorCode.PAYMENT_NOT_COMPLETED);
        }
        this.status = OrderStatus.PENDING;
        updateAudit(updatedBy);
    }

    /**
     * 가게 확인 처리
     */
    public void confirm(LocalDateTime confirmedAt, String updatedBy) {
        validateStatusTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
        updateAudit(updatedBy);
    }

    /**
     * 조리 시작
     */
    public void startPreparing(String updatedBy) {
        validateStatusTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
        updateAudit(updatedBy);
    }

    /**
     * 배달 시작
     */
    public void startDelivering(String updatedBy) {
        validateStatusTransition(OrderStatus.DELIVERING);
        this.status = OrderStatus.DELIVERING;
        updateAudit(updatedBy);
    }

    /**
     * 주문 완료
     */
    public void complete(LocalDateTime completedAt, String updatedBy) {
        validateStatusTransition(OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
        this.completedAt = completedAt;
        updateAudit(updatedBy);
    }

    /**
     * 주문 취소
     */
    public void cancel(String cancelReason, LocalDateTime canceledAt, String updatedBy) {
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new OrderException(OrderErrorCode.CANCEL_REASON_REQUIRED);
        }

        // 취소 가능 여부 검증
        validateCancelable(canceledAt);

        this.status = OrderStatus.CANCELED;
        this.cancelReason = cancelReason;
        this.canceledAt = canceledAt;
        updateAudit(updatedBy);
    }

    /**
     * 취소 가능 여부 검증
     */
    private void validateCancelable(LocalDateTime canceledAt) {
        // 이미 취소된 주문
        if (this.status == OrderStatus.CANCELED) {
            throw new OrderException(OrderErrorCode.ALREADY_CANCELED);
        }

        // 배달 중이거나 완료된 주문은 취소 불가
        if (this.status == OrderStatus.DELIVERING || this.status == OrderStatus.COMPLETED) {
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER,
                    "배달 중이거나 완료된 주문은 취소할 수 없습니다.");
        }

        // 결제 완료 후 5분 경과 확인
        if (this.paymentCompletedAt != null && !isWithinCancelableTime(canceledAt)) {
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER,
                    "결제 완료 후 5분이 경과하여 취소할 수 없습니다.");
        }
    }

    /**
     * 취소 가능 시간 내인지 확인 (결제 완료 후 5분 이내)
     */
    private boolean isWithinCancelableTime(LocalDateTime canceledAt) {
        if (this.paymentCompletedAt == null || canceledAt == null) {
            return true;
        }

        LocalDateTime cancelDeadline = this.paymentCompletedAt.plusMinutes(CANCEL_AVAILABLE_MINUTES);
        return canceledAt.isBefore(cancelDeadline) || canceledAt.isEqual(cancelDeadline);
    }

    /**
     * 상태 전환 유효성 검증
     */
    private void validateStatusTransition(OrderStatus newStatus) {
        switch (newStatus) {
            case PAYMENT_COMPLETED:
                if (this.status != OrderStatus.PAYMENT_PENDING) {
                    throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
                }
                break;
            case CONFIRMED:
                if (this.status != OrderStatus.PENDING) {
                    throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
                }
                break;
            case PREPARING:
                if (this.status != OrderStatus.CONFIRMED) {
                    throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
                }
                break;
            case DELIVERING:
                if (this.status != OrderStatus.PREPARING) {
                    throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
                }
                break;
            case COMPLETED:
                if (this.status != OrderStatus.DELIVERING) {
                    throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
                }
                break;
        }
    }

    /**
     * 소프트 삭제
     */
    public void softDelete(String deletedBy, LocalDateTime deletedAt) {
        if (this.status != OrderStatus.CANCELED && this.status != OrderStatus.COMPLETED) {
            throw new OrderException(OrderErrorCode.CANNOT_CANCEL_ORDER,
                    "완료되거나 취소된 주문만 삭제할 수 있습니다.");
        }
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        updateAudit(deletedBy);
    }

    /**
     * 감사 필드 업데이트
     */
    private void updateAudit(String updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    /**
     * 주문자 검증 (권한 체크용)
     */
    public boolean isOrderedBy(String userId) {
        return this.orderer.getUserId().equals(userId);
    }

    /**
     * 취소 가능 여부 확인 (외부에서 호출 가능)
     */
    public boolean isCancelable() {
        return isCancelableAt(LocalDateTime.now());
    }

    /**
     * 특정 시점에 취소 가능한지 확인
     */
    public boolean isCancelableAt(LocalDateTime targetTime) {
        // 이미 취소되었거나 완료된 주문
        if (this.status == OrderStatus.CANCELED
                || this.status == OrderStatus.COMPLETED
                || this.status == OrderStatus.DELIVERING) {
            return false;
        }

        // 결제 완료 시간이 없으면 취소 가능 (결제 대기 중)
        if (this.paymentCompletedAt == null) {
            return true;
        }

        // 결제 완료 후 5분 이내인지 확인
        return isWithinCancelableTime(targetTime);
    }

    /**
     * 결제 완료 후 남은 취소 가능 시간 (초 단위)
     * @return 남은 시간(초), 취소 불가능하면 0
     */
    public long getRemainingCancelableSeconds() {
        if (this.paymentCompletedAt == null || !isCancelable()) {
            return 0L;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelDeadline = this.paymentCompletedAt.plusMinutes(CANCEL_AVAILABLE_MINUTES);

        if (now.isAfter(cancelDeadline)) {
            return 0L;
        }

        return java.time.Duration.between(now, cancelDeadline).getSeconds();
    }

    /**
     * items의 불변성을 위한 방어적 복사
     */
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
}