package xyz.sparta_project.manjok.domain.payment.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCanceledEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCompletedEvent;

/**
 * Payment 이벤트 발행자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 결제 완료 이벤트 발행
     */
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 발행 - paymentId: {}, orderId: {}",
                event.getPaymentId(), event.getOrderId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 결제 취소 완료 이벤트 발행
     */
    public void publishPaymentCanceled(PaymentCanceledEvent event) {
        log.info("결제 취소 이벤트 발행 - paymentId: {}, orderId: {}, refundAmount: {}",
                event.getPaymentId(), event.getOrderId(), event.getRefundAmount());
        eventPublisher.publishEvent(event);
    }
}