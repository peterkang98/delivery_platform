package xyz.sparta_project.manjok.domain.order.application.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCanceledEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandler;
import xyz.sparta_project.manjok.global.infrastructure.event.handler.EventHandlerProcessor;

/**
 * 결제 취소 완료 이벤트 핸들러 (Payment → Order)
 * Payment 도메인이 환불 처리를 완료한 후 발행한 이벤트를 수신
 */
@Slf4j
@Component
@EventHandler(eventType = PaymentCanceledEvent.class)
@RequiredArgsConstructor
public class PaymentCanceledEventHandler implements EventHandlerProcessor<PaymentCanceledEvent> {

    private final OrderCommandService orderCommandService;

    @Override
    public void handle(PaymentCanceledEvent event) throws Exception {
        log.info("결제 취소 완료 이벤트 수신: orderId={}, paymentId={}, isRefundSuccessful={}",
                event.getOrderId(), event.getPaymentId(), event.getIsRefundSuccessful());

        try {
            if (Boolean.TRUE.equals(event.getIsRefundSuccessful())) {
                // 환불 성공 - Order 상태 업데이트 (취소 완료)
                orderCommandService.completeOrderCancellation(
                        event.getOrderId(),
                        event.getPaymentId(),
                        event.getCancelReason(),
                        event.getUserId()
                );

                log.info("결제 취소 완료 이벤트 처리 성공: orderId={}", event.getOrderId());
            } else {
                // 환불 실패 처리
                log.error("결제 환불 실패: orderId={}, paymentId={}, reason={}",
                        event.getOrderId(), event.getPaymentId(), event.getRefundFailureReason());

                // 환불 실패 시 보상 로직 (예: 관리자 알림, 재시도 큐에 추가 등)
                orderCommandService.handleRefundFailure(
                        event.getOrderId(),
                        event.getRefundFailureReason(),
                        event.getUserId()
                );
            }

        } catch (Exception e) {
            log.error("결제 취소 완료 이벤트 처리 실패: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
}