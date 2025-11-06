package xyz.sparta_project.manjok.domain.order.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderCancelRequestedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderPaymentRequestedEvent;
import xyz.sparta_project.manjok.domain.order.domain.model.Order;
import xyz.sparta_project.manjok.global.infrastructure.event.service.EventPublisher;

import java.time.LocalDateTime;

/**
 * Order 도메인 이벤트 발행자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * 주문 결제 요청 이벤트 발행 (Order → Payment)
     */
    public void publishPaymentRequested(Order order, String paymentKey) {
        // 주문명 생성 (첫 번째 메뉴명 + 외 N건)
        String orderName = createOrderName(order);

        OrderPaymentRequestedEvent event = OrderPaymentRequestedEvent.builder()
                .orderId(order.getId())
                .userId(order.getOrderer().getUserId())
                .userName(order.getOrderer().getName())
                .userPhone(order.getOrderer().getPhone())
                .paymentKey(paymentKey)
                .totalAmount(order.getTotalPrice())
                .orderName(orderName)
                .deliveryAddress(order.getOrderer().getAddress().getFullAddress())
                .deliveryRequest(order.getOrderer().getDeliveryRequest())
                .build();

        eventPublisher.publish(event);

        log.info("주문 결제 요청 이벤트 발행: orderId={}, amount={}",
                order.getId(), order.getTotalPrice());
    }

    /**
     * 주문 취소 요청 이벤트 발행 (Order → Payment)
     */
    public void publishOrderCancelRequested(Order order, String cancelReason, String canceledBy) {
        OrderCancelRequestedEvent event = OrderCancelRequestedEvent.builder()
                .orderId(order.getId())
                .paymentId(order.getPayment().getPaymentId())
                .userId(order.getOrderer().getUserId())
                .refundAmount(order.getTotalPrice())
                .cancelReason(cancelReason)
                .cancelRequestedAt(LocalDateTime.now())
                .canceledBy(canceledBy)
                .build();

        eventPublisher.publish(event);

        log.info("주문 취소 요청 이벤트 발행: orderId={}, paymentId={}, amount={}",
                order.getId(), order.getPayment().getPaymentId(), order.getTotalPrice());
    }

    /**
     * 주문명 생성
     */
    private String createOrderName(Order order) {
        if (order.getItems().isEmpty()) {
            return "주문";
        }

        String firstMenuName = order.getItems().get(0).getMenuName();
        int itemCount = order.getItems().size();

        if (itemCount == 1) {
            return firstMenuName;
        }

        return String.format("%s 외 %d건", firstMenuName, itemCount - 1);
    }
}