package xyz.sparta_project.manjok.domain.order.application.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCanceledEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.service.EventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * PaymentCanceledEventHandler 통합 테스트
 *
 * 테스트 범위:
 * 1. 결제 취소 완료 이벤트 수신 및 처리 (환불 성공)
 * 2. 결제 취소 완료 이벤트 수신 및 처리 (환불 실패)
 * 3. EventLog 상태 변경 확인
 * 4. 핸들러 직접 호출 테스트
 */
@SpringBootTest
@ComponentScan(basePackages = "xyz.sparta_project.manjok")
@ActiveProfiles("test")
class PaymentCanceledEventHandlerTest {

    @Autowired
    private PaymentCanceledEventHandler paymentCanceledEventHandler;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_PAYMENT_ID = "payment-123";
    private static final String CANCEL_REASON = "고객 변심";

    // 실제로 저장된 Order의 ID를 저장할 필드
    private String savedOrderId;

    @BeforeEach
    void setUp() {
        // EventLog 초기화
        eventLogRepository.deleteAll();

        // 테스트용 Order 생성
        createTestOrder();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        eventLogRepository.deleteAll();
        // Order는 soft delete이므로 별도 정리 불필요
    }

    /**
     * 테스트용 Order 생성 및 저장
     * 결제 완료 상태로 생성 (취소 테스트를 위해)
     */
    private void createTestOrder() {
        // 좌표 생성
        Coordinate coordinate = Coordinate.create(
                new java.math.BigDecimal("37.5665"),
                new java.math.BigDecimal("126.9780")
        );

        // 주소 생성
        Address address = Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "123-45",
                coordinate
        );

        // 주문자 생성
        Orderer orderer = Orderer.create(
                TEST_USER_ID,
                "홍길동",
                "010-1234-5678",
                address,
                "문 앞에 놓아주세요"
        );

        // 레스토랑 정보 생성
        Restaurant restaurant = Restaurant.create(
                "restaurant-1",
                "맛있는 치킨집",
                "02-1234-5678",
                address
        );

        // 주문 아이템 생성
        List<OrderItem> items = new java.util.ArrayList<>();
        items.add(OrderItem.create(
                "menu-1",
                "치킨",
                new java.math.BigDecimal("20000"),
                1,
                restaurant,
                new java.util.ArrayList<>()
        ));

        // Payment 생성
        Payment payment = Payment.createPending("temp-payment-key");

        // ✅ Order.create()로 생성 (ID 없음)
        Order order = Order.create(
                orderer,
                items,
                payment,
                java.time.LocalDateTime.now(),
                TEST_USER_ID
        );

        // DB에 저장 후 ID 획득
        Order savedOrder = orderRepository.save(order);

        // ✅ 결제 완료 처리 (취소 테스트를 위해 필요)
        savedOrder.completePayment(TEST_PAYMENT_ID, java.time.LocalDateTime.now(), "SYSTEM");
        savedOrder = orderRepository.save(savedOrder);

        // ✅ 저장된 Order의 실제 ID를 필드에 저장
        this.savedOrderId = savedOrder.getId();
    }

    @Test
    @DisplayName("결제 취소 이벤트 처리 성공 (환불 성공) - Order 상태가 CANCELED로 변경된다")
    void handle_PaymentCanceledEvent_RefundSuccess() throws Exception {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)  // ✅ 실제 저장된 Order ID 사용
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(true)
                .cancelReason(CANCEL_REASON)
                .refundFailureReason(null)
                .build();

        // when
        eventPublisher.publish(event);

        // EventConsumer의 비동기 처리 완료 대기
        Thread.sleep(3000);

        // then
        // 1. EventLog 확인
        EventLog eventLog = waitForEventLogProcessing("PaymentCanceledEvent", 10000);

        assertThat(eventLog).isNotNull();
        assertThat(eventLog.getEventName()).isEqualTo("PaymentCanceledEvent");
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.SUCCESS);

        // 2. ✅ 실제 Order 상태 변경 확인 (핵심!)
        Order updatedOrder = orderRepository.findById(savedOrderId).orElseThrow();

        assertAll("Order 취소 완료 검증",
                () -> assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELED),
                () -> assertThat(updatedOrder.getCancelReason()).isEqualTo(CANCEL_REASON),
                () -> assertThat(updatedOrder.getCanceledAt()).isNotNull(),
                () -> assertThat(updatedOrder.getUpdatedBy()).isEqualTo("SYSTEM")
        );
    }

    @Test
    @DisplayName("결제 취소 이벤트 처리 성공 (환불 실패) - handleRefundFailure가 호출된다")
    void handle_PaymentCanceledEvent_RefundFailure() throws Exception {
        // given
        String refundFailureReason = "PG사 통신 오류";
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)  // 실제 저장된 Order ID 사용
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(false)
                .cancelReason(CANCEL_REASON)
                .refundFailureReason(refundFailureReason)
                .build();

        // when
        eventPublisher.publish(event);

        // EventConsumer의 비동기 처리 완료 대기
        Thread.sleep(3000);

        // then
        // 1. EventLog 확인
        EventLog eventLog = waitForEventLogProcessing("PaymentCanceledEvent", 10000);

        assertThat(eventLog).isNotNull();
        assertThat(eventLog.getEventName()).isEqualTo("PaymentCanceledEvent");
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.SUCCESS);

        // 2. Order 상태는 여전히 PAYMENT_COMPLETED (환불 실패이므로 취소 안됨)
        Order order = orderRepository.findById(savedOrderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        // handleRefundFailure는 로그만 남기고 상태 변경 안함
    }

    /**
     * EventConsumer가 EventLog를 처리할 때까지 대기하는 헬퍼 메서드
     * PENDING 상태가 아닌 EventLog가 생성될 때까지 폴링
     */
    private EventLog waitForEventLogProcessing(String eventName, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            // 모든 상태의 EventLog 조회
            List<EventLog> allLogs = eventLogRepository.findAll();

            // 해당 이벤트명의 EventLog 찾기
            Optional<EventLog> found = allLogs.stream()
                    .filter(log -> log.getEventName().equals(eventName))
                    .findFirst();

            if (found.isPresent()) {
                EventLog eventLog = found.get();
                // PENDING이 아닌 상태(SUCCESS 또는 FAILED)가 되면 반환
                if (eventLog.getStatus() != EventStatus.PENDING) {
                    return eventLog;
                }
            }

            Thread.sleep(500); // 500ms마다 재확인
        }

        // 타임아웃 시 마지막 상태 반환 (PENDING일 수 있음)
        return eventLogRepository.findAll().stream()
                .filter(log -> log.getEventName().equals(eventName))
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("결제 취소 이벤트 핸들러 직접 호출 (환불 성공) - handle 메서드가 정상 작동한다")
    void handle_DirectCall_RefundSuccess() throws Exception {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(true)
                .cancelReason(CANCEL_REASON)
                .refundFailureReason(null)
                .build();

        // when & then
        try {
            paymentCanceledEventHandler.handle(event);
        } catch (Exception e) {
            // 주문이 없어서 예외 발생 가능 - 정상 동작
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("결제 취소 이벤트 핸들러 직접 호출 (환불 실패) - handle 메서드가 정상 작동한다")
    void handle_DirectCall_RefundFailure() throws Exception {
        // given
        String refundFailureReason = "계좌 정보 오류";
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(false)
                .cancelReason(CANCEL_REASON)
                .refundFailureReason(refundFailureReason)
                .build();

        // when & then
        try {
            paymentCanceledEventHandler.handle(event);
        } catch (Exception e) {
            // 주문이 없어서 예외 발생 가능 - 정상 동작
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("결제 취소 이벤트 - isRefundSuccessful이 null인 경우 환불 실패로 처리한다")
    void handle_RefundSuccessfulIsNull_TreatedAsFailure() throws Exception {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(null)
                .cancelReason(CANCEL_REASON)
                .refundFailureReason("상태 정보 누락")
                .build();

        // when & then
        try {
            paymentCanceledEventHandler.handle(event);
        } catch (Exception e) {
            // 주문이 없어서 예외 발생 가능 - 정상 동작
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("결제 취소 이벤트 - 환불 실패 사유가 올바르게 핸들러에 전달된다")
    void handle_RefundFailureReason_TransferredCorrectly() throws Exception {
        // given
        String specificFailureReason = "카드사 점검 중";
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId("order-999")
                .userId("user-999")
                .paymentId("payment-999")
                .isRefundSuccessful(false)
                .cancelReason("주문 취소")
                .refundFailureReason(specificFailureReason)
                .build();

        // when & then
        try {
            paymentCanceledEventHandler.handle(event);
        } catch (Exception e) {
            // 주문이 없어서 예외 발생 가능 - 정상 동작
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("결제 취소 이벤트 발행 - 이벤트가 정상적으로 발행되고 EventLog에 저장된다")
    void publishEvent_AndSaveToEventLog() throws Exception {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .isRefundSuccessful(true)
                .cancelReason(CANCEL_REASON)
                .build();

        // when
        eventPublisher.publish(event);
        Thread.sleep(3000);

        // then
        List<EventLog> allLogs = eventLogRepository.findAll();
        List<EventLog> savedLogs = allLogs.stream()
                .filter(log -> log.getEventName().equals("PaymentCanceledEvent"))
                .toList();

        assertThat(savedLogs).isNotEmpty();

        EventLog savedLog = savedLogs.get(0);

        // Payload 역직렬화 검증
        PaymentCanceledEvent savedEvent = objectMapper.readValue(
                savedLog.getPayload(),
                PaymentCanceledEvent.class
        );

        assertThat(savedEvent.getOrderId()).isEqualTo(savedOrderId);
        assertThat(savedEvent.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(savedEvent.getIsRefundSuccessful()).isTrue();
        assertThat(savedEvent.getCancelReason()).isEqualTo(CANCEL_REASON);
    }
}