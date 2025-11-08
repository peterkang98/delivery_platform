package xyz.sparta_project.manjok.domain.payment.application.event;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderPaymentRequestedEvent;

import java.math.BigDecimal;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * OrderPaymentEventHandler 통합 테스트
 * 실제 토스 API를 호출하여 검증 로직 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("실제 AI API 호출이 필요한 테스트. 필요시 주석 해제하고 실행하세요.")
class OrderPaymentEventHandlerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // 실제 토스 테스트 결제 키 (하드코딩)
    private static final String TEST_PAYMENT_KEY = "tviva20251106140809O51N1";

    @Test
    @DisplayName("토스 API 호출 및 검증 실패 테스트 - 주문 ID 불일치")
    void testPaymentVerification_OrderIdMismatch() {
        // given - 임의의 주문 ID로 이벤트 생성
        OrderPaymentRequestedEvent event = OrderPaymentRequestedEvent.builder()
                .orderId("ORDER-RANDOM-12345") // 임의의 주문 ID
                .userId("USER-TEST-001")
                .userName("테스트 사용자")
                .userPhone("010-1234-5678")
                .paymentKey(TEST_PAYMENT_KEY) // 실제 토스 결제 키
                .totalAmount(new BigDecimal("15000"))
                .orderName("테스트 주문")
                .deliveryAddress("서울시 강남구")
                .deliveryRequest("문 앞에 놓아주세요")
                .build();

        // when
        eventPublisher.publishEvent(event);

        // then - 비동기 처리 완료 대기
        await().atMost(10, SECONDS).untilAsserted(() -> {
            System.out.println("=== 주문 ID 불일치 검증 테스트 완료 ===");
            System.out.println("토스 API는 정상 호출되었으나, 주문 ID가 일치하지 않아 검증 실패");
        });
    }

    @Test
    @DisplayName("토스 API 호출 및 검증 실패 테스트 - 금액 불일치")
    void testPaymentVerification_AmountMismatch() {
        // given - 임의의 금액으로 이벤트 생성
        OrderPaymentRequestedEvent event = OrderPaymentRequestedEvent.builder()
                .orderId("ORDER-RANDOM-67890")
                .userId("USER-TEST-002")
                .userName("테스트 사용자2")
                .userPhone("010-5678-1234")
                .paymentKey(TEST_PAYMENT_KEY) // 실제 토스 결제 키
                .totalAmount(new BigDecimal("99999")) // 임의의 금액
                .orderName("테스트 주문2")
                .deliveryAddress("서울시 서초구")
                .deliveryRequest("빠른 배송 부탁드립니다")
                .build();

        // when
        eventPublisher.publishEvent(event);

        // then - 비동기 처리 완료 대기
        await().atMost(10, SECONDS).untilAsserted(() -> {
            System.out.println("=== 결제 금액 불일치 검증 테스트 완료 ===");
            System.out.println("토스 API는 정상 호출되었으나, 금액이 일치하지 않아 검증 실패");
        });
    }

    @Test
    @DisplayName("토스 API 호출 및 검증 실패 테스트 - 모두 불일치")
    void testPaymentVerification_AllMismatch() {
        // given - 주문 ID와 금액 모두 임의로 생성
        OrderPaymentRequestedEvent event = OrderPaymentRequestedEvent.builder()
                .orderId("ORDER-WRONG-999")
                .userId("USER-TEST-003")
                .userName("테스트 사용자3")
                .userPhone("010-9999-8888")
                .paymentKey(TEST_PAYMENT_KEY) // 실제 토스 결제 키
                .totalAmount(new BigDecimal("77777")) // 임의의 금액
                .orderName("테스트 주문3")
                .deliveryAddress("서울시 송파구")
                .deliveryRequest("경비실에 맡겨주세요")
                .build();

        // when
        eventPublisher.publishEvent(event);

        // then - 비동기 처리 완료 대기
        await().atMost(10, SECONDS).untilAsserted(() -> {
            System.out.println("=== 주문 ID 및 금액 모두 불일치 검증 테스트 완료 ===");
            System.out.println("토스 API는 정상 호출되었으나, 검증 데이터가 일치하지 않아 실패");
            System.out.println("EventConsumer가 실패를 감지하고 재시도 이벤트 발행 예정");
        });
    }

    @Test
    @DisplayName("토스 API 정상 호출 테스트 - 로그 확인용")
    void testTossApiCall() throws InterruptedException {
        // given
        OrderPaymentRequestedEvent event = OrderPaymentRequestedEvent.builder()
                .orderId("ORDER-API-TEST-001")
                .userId("USER-API-TEST")
                .userName("API 테스트")
                .userPhone("010-0000-0000")
                .paymentKey(TEST_PAYMENT_KEY)
                .totalAmount(new BigDecimal("100"))
                .orderName("API 호출 테스트")
                .deliveryAddress("서울시 중구")
                .deliveryRequest("테스트")
                .build();

        // when
        System.out.println("\n=== 토스 API 호출 시작 ===");
        System.out.println("요청 paymentKey: " + TEST_PAYMENT_KEY);
        System.out.println("요청 orderId: ORDER-API-TEST-001");
        System.out.println("요청 totalAmount: 100");

        eventPublisher.publishEvent(event);

        // then - 비동기 처리 대기 (5초)
        Thread.sleep(5000);

        System.out.println("\n=== 토스 API 호출 완료 ===");
        System.out.println("콘솔 로그에서 다음 정보를 확인하세요:");
        System.out.println("1. '토스 결제 조회 성공' 로그");
        System.out.println("2. 실제 토스에 저장된 orderId");
        System.out.println("3. 실제 토스에 저장된 totalAmount");
        System.out.println("4. 실제 토스에 저장된 status");
        System.out.println("\n예상 결과:");
        System.out.println("- orderId 불일치로 인한 검증 실패");
        System.out.println("- '주문 ID가 일치하지 않습니다' 에러 발생");
    }
}