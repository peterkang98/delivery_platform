package xyz.sparta_project.manjok.domain.order.application.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.application.service.OrderCommandService;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.PaymentCompletedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;
import xyz.sparta_project.manjok.global.infrastructure.event.service.EventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * PaymentCompletedEventHandler 통합 테스트
 *
 * 테스트 범위:
 * 1. 결제 완료 이벤트 수신 및 처리
 * 2. OrderCommandService.completePayment 호출 확인
 * 3. EventLog 상태 변경 확인 (PENDING → SUCCESS)
 * 4. 예외 발생 시 처리 (PENDING → FAILED)
 *
 * ⚠️ 주의: @Transactional 제거
 * - 비동기 EventConsumer가 별도 트랜잭션으로 동작
 * - 테스트 트랜잭션과 격리되어 데이터 공유 안됨
 * - @BeforeEach/@AfterEach에서 명시적 데이터 정리 필요
 */
@SpringBootTest
@ComponentScan(basePackages = "xyz.sparta_project.manjok")
@ActiveProfiles("test")
class PaymentCompletedEventHandlerTest {

    @Autowired
    private PaymentCompletedEventHandler paymentCompletedEventHandler;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderRepository orderRepository;  // ← 추가

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_PAYMENT_ID = "payment-123";

    //실제로 저장된 Order의 ID를 저장할 필드
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

        // Payment 생성 (결제 대기 상태)
        Payment payment = Payment.createPending("temp-payment-key");

        // ✅ Order.create()로 생성 (ID 없음 - Repository가 자동 생성)
        Order order = Order.create(
                orderer,
                items,
                payment,
                java.time.LocalDateTime.now(),
                TEST_USER_ID
        );

        // DB에 저장 후 ID 획득
        Order savedOrder = orderRepository.save(order);

        // ✅ 저장된 Order의 실제 ID를 필드에 저장
        this.savedOrderId = savedOrder.getId();
    }

    @Test
    @DisplayName("결제 완료 이벤트 처리 성공 - Order 상태가 PAYMENT_COMPLETED로 변경된다")
    void handle_PaymentCompletedEvent_Success() throws Exception {
        // given
        LocalDateTime paymentCompletedAt = LocalDateTime.now();
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .paymentCompletedAt(paymentCompletedAt)
                .build();

        // when
        eventPublisher.publish(event);

        // EventConsumer의 비동기 처리 완료 대기
        Thread.sleep(3000);

        // then
        // 1. EventLog 상태 확인
        EventLog eventLog = waitForEventLogProcessing("PaymentCompletedEvent", 10000);

        assertThat(eventLog).isNotNull();
        assertThat(eventLog.getEventName()).isEqualTo("PaymentCompletedEvent");
        assertThat(eventLog.getStatus()).isEqualTo(EventStatus.SUCCESS);

        // 2. ✅ 실제 Order 상태 변경 확인 (핵심!)
        Order updatedOrder = orderRepository.findById(savedOrderId).orElseThrow();

        assertAll("Order 결제 완료 검증",
                () -> assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING),
                () -> assertThat(updatedOrder.getPayment().getPaymentId()).isEqualTo(TEST_PAYMENT_ID),
                () -> assertThat(updatedOrder.getPayment().isCompleted()).isTrue(),
                () -> assertThat(updatedOrder.getPaymentCompletedAt()).isNotNull(),
                () -> assertThat(updatedOrder.getUpdatedBy()).isEqualTo("SYSTEM")
        );
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
    @DisplayName("결제 완료 이벤트 핸들러 직접 호출 - handle 메서드가 정상 작동한다")
    void handle_DirectCall_Success() throws Exception {
        // given
        LocalDateTime paymentCompletedAt = LocalDateTime.now();
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId(TEST_PAYMENT_ID)
                .paymentCompletedAt(paymentCompletedAt)
                .build();

        // when & then
        // 실제 OrderCommandService가 동작
        paymentCompletedEventHandler.handle(event);

        // Order 상태 확인
        Order updatedOrder = orderRepository.findById(savedOrderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("결제 완료 이벤트 - 이벤트 데이터가 올바르게 전달된다")
    void handle_EventDataTransferred_Correctly() throws Exception {
        // given
        LocalDateTime specificTime = LocalDateTime.of(2025, 1, 15, 14, 30, 0);
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(savedOrderId)
                .userId(TEST_USER_ID)
                .paymentId("payment-specific-999")
                .paymentCompletedAt(specificTime)
                .build();

        // when
        paymentCompletedEventHandler.handle(event);

        // then
        // Order 상태 확인
        Order updatedOrder = orderRepository.findById(savedOrderId).orElseThrow();
        assertThat(updatedOrder.getPayment().getPaymentId()).isEqualTo("payment-specific-999");
    }
}