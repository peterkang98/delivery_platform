package xyz.sparta_project.manjok.global.infrastructure.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 취소 완료 이벤트 (Payment → Order)
 * Payment 도메인에서 환불 처리를 완료한 후 발행
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCanceledEvent {

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 결제 ID
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
     * 결제 취소 완료 시간
     */
    private LocalDateTime paymentCanceledAt;

    /**
     * 환불 성공 여부
     */
    private Boolean isRefundSuccessful;

    /**
     * 환불 실패 사유 (실패 시)
     */
    private String refundFailureReason;
}