package xyz.sparta_project.manjok.domain.payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.sparta_project.manjok.domain.payment.domain.client.PaymentClient;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentErrorCode;
import xyz.sparta_project.manjok.domain.payment.domain.exception.PaymentException;
import xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto.TossPaymentResponse;

import java.math.BigDecimal;

/**
 * 결제 검증 서비스
 * 토스 API를 통한 결제 정보 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationService {

    private final PaymentClient paymentClient;

    /**
     * 토스 결제 정보 조회 및 검증
     *
     * @param paymentKey 토스 결제 키
     * @param orderId 주문 ID
     * @param expectedAmount 예상 결제 금액
     * @return 검증된 토스 결제 정보
     */
    public TossPaymentResponse verifyPayment(
            String paymentKey,
            String orderId,
            BigDecimal expectedAmount
    ) {
        log.info("결제 검증 시작 - paymentKey: {}, orderId: {}, expectedAmount: {}",
                paymentKey, orderId, expectedAmount);

        // 1. 토스 결제 조회
        TossPaymentResponse response = paymentClient.getPayment(paymentKey);

        // 2. 주문 ID 일치 확인
//        if (!orderId.equals(response.getOrderId())) {
//            log.error("주문 ID 불일치 - expected: {}, actual: {}", orderId, response.getOrderId());
//            throw new PaymentException(
//                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
//                    "주문 ID가 일치하지 않습니다."
//            );
//        }

        // 3. 결제 금액 일치 확인
        if (expectedAmount.compareTo(response.getTotalAmount()) != 0) {
            log.error("결제 금액 불일치 - expected: {}, actual: {}",
                    expectedAmount, response.getTotalAmount());
            throw new PaymentException(
                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
                    "결제 금액이 일치하지 않습니다."
            );
        }

        // 4. 결제 상태 확인 (DONE 상태여야 함)
//        if (!response.isDone()) {
//            log.error("결제 미완료 상태 - status: {}", response.getStatus());
//            throw new PaymentException(
//                    PaymentErrorCode.PAYMENT_VERIFICATION_FAILED,
//                    "결제가 완료되지 않았습니다. 현재 상태: " + response.getStatus()
//            );
//        }

        log.info("결제 검증 완료 - paymentKey: {}", paymentKey);
        return response;
    }

    /**
     * 결제 키 검증만 수행 (간단 검증)
     *
     * @param paymentKey 토스 결제 키
     * @return 토스 결제 정보
     */
    public TossPaymentResponse verifyPaymentKey(String paymentKey) {
        log.info("결제 키 검증 - paymentKey: {}", paymentKey);

        TossPaymentResponse response = paymentClient.getPayment(paymentKey);

        log.info("결제 키 검증 완료 - paymentKey: {}, status: {}",
                paymentKey, response.getStatus());

        return response;
    }
}