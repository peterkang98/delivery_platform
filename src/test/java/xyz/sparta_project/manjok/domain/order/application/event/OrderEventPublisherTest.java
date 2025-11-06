package xyz.sparta_project.manjok.domain.order.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderCancelRequestedEvent;
import xyz.sparta_project.manjok.global.infrastructure.event.dto.OrderPaymentRequestedEvent;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventLog;
import xyz.sparta_project.manjok.global.infrastructure.event.domain.EventStatus;
import xyz.sparta_project.manjok.global.infrastructure.event.repository.EventLogRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * OrderEventPublisher 통합 테스트
 *
 * 테스트 범위:
 * 1. 주문 결제 요청 이벤트 발행 (Order → Payment)
 * 2. 주문 취소 요청 이벤트 발행 (Order → Payment)
 * 3. EventLog 저장 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderEventPublisherTest {

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_PAYMENT_KEY = "test-payment-key";

    @BeforeEach
    void setUp() {
        // 테스트용 주문 객체 생성
        testOrder = createTestOrder();
    }

    @Test
    @DisplayName("주문 결제 요청 이벤트 발행 - 이벤트가 정상적으로 발행되고 EventLog에 저장된다")
    void publishPaymentRequested_Success() throws Exception {
        // given
        String paymentKey = TEST_PAYMENT_KEY;

        // when
        orderEventPublisher.publishPaymentRequested(testOrder, paymentKey);

        // 이벤트 처리를 위한 대기 시간
        Thread.sleep(1000);

        // then
        List<EventLog> eventLogs = eventLogRepository.findAllByStatus(EventStatus.PENDING);

        assertAll(
                () -> assertThat(eventLogs).isNotEmpty(),
                () -> {
                    EventLog savedEventLog = eventLogs.stream()
                            .filter(log -> log.getEventName().equals("OrderPaymentRequestedEvent"))
                            .findFirst()
                            .orElseThrow();

                    assertThat(savedEventLog.getEventName()).isEqualTo("OrderPaymentRequestedEvent");
                    assertThat(savedEventLog.getStatus()).isEqualTo(EventStatus.PENDING);

                    // payload 검증
                    OrderPaymentRequestedEvent savedEvent = objectMapper.readValue(
                            savedEventLog.getPayload(),
                            OrderPaymentRequestedEvent.class
                    );

                    assertThat(savedEvent.getOrderId()).isEqualTo(testOrder.getId());
                    assertThat(savedEvent.getUserId()).isEqualTo(TEST_USER_ID);
                    assertThat(savedEvent.getPaymentKey()).isEqualTo(paymentKey);
                    assertThat(savedEvent.getTotalAmount()).isEqualByComparingTo(testOrder.getTotalPrice());
                    assertThat(savedEvent.getOrderName()).contains("치킨");
                }
        );
    }

    @Test
    @DisplayName("주문 결제 요청 이벤트 발행 - 주문명이 올바르게 생성된다 (단일 메뉴)")
    void publishPaymentRequested_SingleItem_OrderNameCorrect() throws Exception {
        // given
        Order singleItemOrder = createTestOrderWithSingleItem();
        String paymentKey = TEST_PAYMENT_KEY;

        // when
        orderEventPublisher.publishPaymentRequested(singleItemOrder, paymentKey);
        Thread.sleep(1000);

        // then
        List<EventLog> eventLogs = eventLogRepository.findAllByStatus(EventStatus.PENDING);
        EventLog savedEventLog = eventLogs.stream()
                .filter(log -> log.getEventName().equals("OrderPaymentRequestedEvent"))
                .findFirst()
                .orElseThrow();

        OrderPaymentRequestedEvent savedEvent = objectMapper.readValue(
                savedEventLog.getPayload(),
                OrderPaymentRequestedEvent.class
        );

        assertThat(savedEvent.getOrderName()).isEqualTo("치킨");
    }

    @Test
    @DisplayName("주문 결제 요청 이벤트 발행 - 주문명이 올바르게 생성된다 (다중 메뉴)")
    void publishPaymentRequested_MultipleItems_OrderNameCorrect() throws Exception {
        // given
        String paymentKey = TEST_PAYMENT_KEY;

        // when
        orderEventPublisher.publishPaymentRequested(testOrder, paymentKey);
        Thread.sleep(1000);

        // then
        List<EventLog> eventLogs = eventLogRepository.findAllByStatus(EventStatus.PENDING);
        EventLog savedEventLog = eventLogs.stream()
                .filter(log -> log.getEventName().equals("OrderPaymentRequestedEvent"))
                .findFirst()
                .orElseThrow();

        OrderPaymentRequestedEvent savedEvent = objectMapper.readValue(
                savedEventLog.getPayload(),
                OrderPaymentRequestedEvent.class
        );

        // "치킨 외 1건" 형식 확인
        assertThat(savedEvent.getOrderName()).matches("치킨 외 \\d+건");
    }

    @Test
    @DisplayName("주문 취소 요청 이벤트 발행 - 이벤트가 정상적으로 발행되고 EventLog에 저장된다")
    void publishOrderCancelRequested_Success() throws Exception {
        // given
        String cancelReason = "고객 변심";
        String canceledBy = TEST_USER_ID;

        // 결제 완료 상태로 변경
        testOrder.completePayment("payment-123", LocalDateTime.now(), TEST_USER_ID);

        // when
        orderEventPublisher.publishOrderCancelRequested(testOrder, cancelReason, canceledBy);
        Thread.sleep(1000);

        // then
        List<EventLog> eventLogs = eventLogRepository.findAllByStatus(EventStatus.PENDING);

        assertAll(
                () -> assertThat(eventLogs).isNotEmpty(),
                () -> {
                    EventLog savedEventLog = eventLogs.stream()
                            .filter(log -> log.getEventName().equals("OrderCancelRequestedEvent"))
                            .findFirst()
                            .orElseThrow();

                    assertThat(savedEventLog.getEventName()).isEqualTo("OrderCancelRequestedEvent");
                    assertThat(savedEventLog.getStatus()).isEqualTo(EventStatus.PENDING);

                    // payload 검증
                    OrderCancelRequestedEvent savedEvent = objectMapper.readValue(
                            savedEventLog.getPayload(),
                            OrderCancelRequestedEvent.class
                    );

                    assertThat(savedEvent.getOrderId()).isEqualTo(testOrder.getId());
                    assertThat(savedEvent.getPaymentId()).isEqualTo("payment-123");
                    assertThat(savedEvent.getUserId()).isEqualTo(TEST_USER_ID);
                    assertThat(savedEvent.getRefundAmount()).isEqualByComparingTo(testOrder.getTotalPrice());
                    assertThat(savedEvent.getCancelReason()).isEqualTo(cancelReason);
                    assertThat(savedEvent.getCanceledBy()).isEqualTo(canceledBy);
                    assertThat(savedEvent.getCancelRequestedAt()).isNotNull();
                }
        );
    }

    @Test
    @DisplayName("주문 취소 요청 이벤트 발행 - 배송 정보가 포함된다")
    void publishOrderCancelRequested_WithDeliveryInfo() throws Exception {
        // given
        String cancelReason = "배송지 변경";
        String canceledBy = TEST_USER_ID;
        testOrder.completePayment("payment-123", LocalDateTime.now(), TEST_USER_ID);

        // when
        orderEventPublisher.publishOrderCancelRequested(testOrder, cancelReason, canceledBy);
        Thread.sleep(1000);

        // then
        List<EventLog> eventLogs = eventLogRepository.findAllByStatus(EventStatus.PENDING);
        EventLog savedEventLog = eventLogs.stream()
                .filter(log -> log.getEventName().equals("OrderCancelRequestedEvent"))
                .findFirst()
                .orElseThrow();

        OrderCancelRequestedEvent savedEvent = objectMapper.readValue(
                savedEventLog.getPayload(),
                OrderCancelRequestedEvent.class
        );

        assertThat(savedEvent.getRefundAmount()).isEqualByComparingTo(testOrder.getTotalPrice());
    }

    // ==================== Helper Methods ====================

    private Order createTestOrder() {
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );

        Address address = Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "123-45",
                coordinate
        );

        Orderer orderer = Orderer.create(
                TEST_USER_ID,
                "홍길동",
                "010-1234-5678",
                address,
                "문 앞에 놓아주세요"
        );

        Restaurant restaurant = Restaurant.create(
                "restaurant-1",
                "맛있는 치킨집",
                "02-1234-5678",
                address
        );

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.create(
                "menu-1",
                "치킨",
                new BigDecimal("20000"),
                1,
                restaurant,
                new ArrayList<>()
        ));
        items.add(OrderItem.create(
                "menu-2",
                "콜라",
                new BigDecimal("2000"),
                1,
                restaurant,
                new ArrayList<>()
        ));

        Payment payment = Payment.createPending("temp-payment-key");

        return Order.create(
                orderer,
                items,
                payment,
                LocalDateTime.now(),
                TEST_USER_ID
        );
    }

    private Order createTestOrderWithSingleItem() {
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );

        Address address = Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "123-45",
                coordinate
        );

        Orderer orderer = Orderer.create(
                TEST_USER_ID,
                "홍길동",
                "010-1234-5678",
                address,
                "문 앞에 놓아주세요"
        );

        Restaurant restaurant = Restaurant.create(
                "restaurant-1",
                "맛있는 치킨집",
                "02-1234-5678",
                address
        );

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.create(
                "menu-1",
                "치킨",
                new BigDecimal("20000"),
                1,
                restaurant,
                new ArrayList<>()
        ));

        Payment payment = Payment.createPending("temp-payment-key");

        return Order.create(
                orderer,
                items,
                payment,
                LocalDateTime.now(),
                TEST_USER_ID
        );
    }
}