package xyz.sparta_project.manjok.domain.payment.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentCommandService;
import xyz.sparta_project.manjok.domain.payment.application.service.PaymentVerificationService;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;
import xyz.sparta_project.manjok.domain.payment.domain.model.Payment;
import xyz.sparta_project.manjok.domain.payment.domain.model.PaymentMethod;
import xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto.TossPaymentResponse;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderPaymentRequestedEvent;

/**
 * 주문 결제 요청 이벤트 핸들러
 * Order 도메인에서 발행한 결제 요청 이벤트를 처리
 *
 * 플로우:
 * 1. OrderPaymentRequestedEvent 수신
 * 2. 토스 결제 조회 API 호출 (/v1/payments/{paymentKey})
 * 3. 응답 totalAmount, orderId, paymentKey 검증
 * 4-a. [검증 성공] PaymentCommandService.createPayment() -> DB 저장 -> PaymentCompletedEvent 발행
 * 4-b. [검증 실패] PaymentCommandService.failPayment() -> 실패 로그 남김
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentEventHandler {

    private final PaymentCommandService paymentCommandService;
    private final PaymentVerificationService verificationService;

    /**
     * 주문 결제 요청 이벤트 처리
     */
    @Async
    @EventListener
    @Transactional
    public void handleOrderPaymentRequested(OrderPaymentRequestedEvent event) {
        log.info("=== 주문 결제 요청 이벤트 수신 - orderId: {}, paymentKey: {} ===",
                event.getOrderId(), event.getPaymentKey());

        Payment payment = null;

        try {
            // STEP 1: 토스 결제 조회 및 검증
            log.info("STEP 1: 토스 결제 조회 시작 - paymentKey: {}", event.getPaymentKey());

            TossPaymentResponse tossResponse = verificationService.verifyPayment(
                    event.getPaymentKey(),
                    event.getOrderId(),
                    event.getTotalAmount()
            );

            log.info("STEP 1: 토스 결제 검증 성공 - paymentKey: {}, status: {}, totalAmount: {}",
                    event.getPaymentKey(), tossResponse.getStatus(), tossResponse.getTotalAmount());

            // STEP 2: Payment 엔티티 생성 및 저장
            log.info("STEP 2: Payment 엔티티 생성 시작");

            payment = paymentCommandService.createPayment(
                    event.getOrderId(),
                    event.getUserId(),
                    event.getPaymentKey(),
                    generatePayToken(tossResponse), // 토스 응답에서 토큰 생성
                    event.getTotalAmount(),
                    convertPaymentMethod(tossResponse.getMethod()),
                    event.getUserId()
            );

            log.info("STEP 2: Payment 엔티티 생성 완료 - paymentId: {}", payment.getId());

            // STEP 3: 결제 승인 처리
            log.info("STEP 3: 결제 승인 처리 시작");

            Payment approvedPayment = paymentCommandService.approvePayment(
                    payment.getId(),
                    event.getUserId()
            );

            log.info("STEP 3: 결제 승인 완료 - paymentId: {}", approvedPayment.getId());
            log.info("=== 주문 결제 요청 처리 완료 - orderId: {}, paymentId: {} ===",
                    event.getOrderId(), approvedPayment.getId());

        } catch (PaymentException e) {
            log.error("=== 결제 검증 실패 - orderId: {}, reason: {} ===",
                    event.getOrderId(), e.getMessage());

            handlePaymentFailure(payment, event, e.getMessage());

        } catch (Exception e) {
            log.error("=== 주문 결제 요청 처리 중 예외 발생 - orderId: {} ===",
                    event.getOrderId(), e);

            handlePaymentFailure(payment, event, "결제 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 결제 실패 처리
     */
    private void handlePaymentFailure(Payment payment, OrderPaymentRequestedEvent event, String reason) {
        try {
            if (payment != null) {
                // 이미 생성된 Payment가 있으면 실패 처리
                paymentCommandService.failPayment(
                        payment.getId(),
                        reason,
                        event.getUserId()
                );
                log.info("결제 실패 처리 완료 - paymentId: {}, reason: {}", payment.getId(), reason);
            } else {
                // Payment가 생성되지 않았으면 로그만 남김
                log.error("결제 생성 전 실패 - orderId: {}, reason: {}", event.getOrderId(), reason);
            }


        } catch (Exception failureException) {
            log.error("결제 실패 처리 중 추가 예외 발생 - orderId: {}",
                    event.getOrderId(), failureException);
        }
    }

    /**
     * 토스 응답에서 결제 토큰 생성
     */
    private String generatePayToken(TossPaymentResponse response) {
        // 실제로는 토스 API 응답의 특정 필드를 사용하거나
        // 별도의 토큰 생성 로직을 구현해야 함
        return "PAY_TOKEN_" + response.getPaymentKey() + "_" + System.currentTimeMillis();
    }

    /**
     * 토스 결제 방법을 PaymentMethod로 변환
     */
    private PaymentMethod convertPaymentMethod(String tossMethod) {
        if (tossMethod == null) {
            return PaymentMethod.CARD;
        }

        return switch (tossMethod.toLowerCase()) {
            case "카드", "card" -> PaymentMethod.CARD;
            case "가상계좌", "virtual_account" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "계좌이체", "transfer" -> PaymentMethod.TRANSFER;
            case "휴대폰", "mobile" -> PaymentMethod.CARD; // 적절한 매핑 필요
            default -> PaymentMethod.CARD;
        };
    }
}