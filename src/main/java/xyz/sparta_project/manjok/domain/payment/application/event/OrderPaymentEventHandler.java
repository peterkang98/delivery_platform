//package xyz.sparta_project.manjok.domain.payment.application.event;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderPaymentRequestedEvent;
//import xyz.sparta_project.manjok.domain.payment.application.service.PaymentCommandService;
//import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentMethod;
//
///**
// * 주문 결제 요청 이벤트 핸들러
// * Order 도메인에서 발행한 결제 요청 이벤트를 처리
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OrderPaymentEventHandler {
//
//    private final PaymentCommandService paymentCommandService;
//
//    /**
//     * 주문 결제 요청 이벤트 처리
//     */
//    @Async
//    @EventListener
//    @Transactional
//    public void handleOrderPaymentRequested(OrderPaymentRequestedEvent event) {
//        log.info("주문 결제 요청 이벤트 수신 - orderId: {}, paymentKey: {}",
//                event.getOrderId(), event.getPaymentKey());
//
//        try {
//            // 결제 생성
//            paymentCommandService.createPayment(
//                    event.getOrderId(),
//                    event.getUserId(),
//                    event.getPaymentKey(),
//                    generatePayToken(event), // 실제로는 토스API에서 받아올 토큰
//                    event.getTotalAmount(),
//                    PaymentMethod.CARD, // 실제로는 이벤트에서 받아와야 함
//                    event.getUserId()
//            );
//
//            // TODO: 토스 결제 승인 API 호출
//            // 실제 구현 시에는 토스 API를 호출하여 결제 승인 처리
//            // 성공 시 paymentCommandService.approvePayment() 호출
//            // 실패 시 paymentCommandService.failPayment() 호출
//
//            log.info("주문 결제 요청 처리 완료 - orderId: {}", event.getOrderId());
//
//        } catch (Exception e) {
//            log.error("주문 결제 요청 처리 실패 - orderId: {}", event.getOrderId(), e);
//            // TODO: 보상 트랜잭션 or 재시도 로직
//        }
//    }
//
//    /**
//     * 결제 토큰 생성 (임시)
//     * 실제로는 토스 API 응답에서 받아와야 함
//     */
//    private String generatePayToken(OrderPaymentRequestedEvent event) {
//        return "PAY_TOKEN_" + event.getOrderId() + "_" + System.currentTimeMillis();
//    }
//}