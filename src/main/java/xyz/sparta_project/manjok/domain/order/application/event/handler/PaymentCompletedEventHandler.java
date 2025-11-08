package xyz.sparta_project.manjok.domain.order.application.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCompletedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

/**
 * 결제 완료 이벤트 핸들러 (Payment → Order)
 * Payment 도메인이 결제 검증 및 저장을 완료한 후 발행한 이벤트를 수신
 */
@Slf4j
@Component
@EventHandler(eventType = PaymentCompletedEvent.class)
@RequiredArgsConstructor
public class PaymentCompletedEventHandler implements EventHandlerProcessor<PaymentCompletedEvent> {

    private final OrderCommandService orderCommandService;

    @Override
    public void handle(PaymentCompletedEvent event) throws Exception {
        log.info("결제 완료 이벤트 수신: orderId={}, paymentId={}",
                event.getOrderId(), event.getPaymentId());

        try {
            // Order 상태 업데이트 (결제 완료)
            orderCommandService.completePayment(
                    event.getOrderId(),
                    event.getPaymentId(),
                    event.getPaymentCompletedAt(),
                    event.getUserId()
            );

            log.info("결제 완료 이벤트 처리 성공: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
}