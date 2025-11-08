package xyz.sparta_project.manjok.global.infrastructure.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 취소 요청 이벤트 (Order → Payment)
 * 주문이 취소되어 결제 환불이 필요할 때 발행
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRequestedEvent {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 결제 ID (Payment 도메인의 엔티티 ID)
     */
    private String paymentId;

    /**
     * 사용자 ID
     */
    private String userId;

    /**
     * 환불 금액
     */
    private BigDecimal refundAmount;

    /**
     * 취소 사유
     */
    private String cancelReason;

    /**
     * 취소 요청 시간
     */
    private LocalDateTime cancelRequestedAt;

    /**
     * 취소 요청자 (사용자 ID 또는 시스템)
     */
    private String canceledBy;
}