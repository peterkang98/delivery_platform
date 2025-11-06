package xyz.sparta_project.manjok.global.infrastructure.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 완료 이벤트 (Payment → Order)
 * Payment 도메인이 결제 완료 후 Order에게 알림
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {

    private String orderId;
    private String userId;
    private String paymentId; // 실제 결제 완료된 Payment ID
    private LocalDateTime paymentCompletedAt;
}