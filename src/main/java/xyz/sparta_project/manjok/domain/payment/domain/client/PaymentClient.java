package xyz.sparta_project.manjok.domain.payment.domain.client;

import xyz.sparta_project.manjok.domain.payment.infrastructure.client.dto.TossPaymentResponse;

/**
 * 결제 클라이언트 인터페이스
 * 도메인 계층에서 외부 결제 시스템과의 통신을 추상화
 */
public interface PaymentClient {

    /**
     * 결제 정보 조회
     *
     * @param paymentKey 토스 결제 키
     * @return 결제 정보
     */
    TossPaymentResponse getPayment(String paymentKey);

    /**
     * 결제 승인
     *
     * @param paymentKey 토스 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 승인된 결제 정보
     */
    TossPaymentResponse approvePayment(String paymentKey, String orderId, String amount);

    /**
     * 결제 취소
     *
     * @param paymentKey 토스 결제 키
     * @param cancelReason 취소 사유
     * @return 취소된 결제 정보
     */
    TossPaymentResponse cancelPayment(String paymentKey, String cancelReason);
}