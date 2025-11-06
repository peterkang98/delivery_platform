package xyz.sparta_project.manjok.global.infrastructure.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 주문 결제 요청 이벤트 (Order → Payment)
 * Payment 도메인에게 결제 처리를 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentRequestedEvent {

    private String orderId;
    private String userId;
    private String userName;
    private String userPhone;

    // 결제 정보
    private String paymentKey; // 토스 ID
    private BigDecimal totalAmount;
    private String orderName; // 주문명 (예: "치킨 외 2건")

    // 배송 정보
    private String deliveryAddress;
    private String deliveryRequest;
}