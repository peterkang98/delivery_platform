package xyz.sparta_project.manjok.domain.order.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.application.event.OrderEventPublisher;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * OrderCommandService 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderCommandService 통합 테스트")
class OrderCommandServiceTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    private String testUserId;
    private String testRestaurantId;
    private Orderer testOrderer;
    private List<OrderItem> testItems;

    @BeforeEach
    void setUp() {
        testUserId = "user-" + System.currentTimeMillis();
        testRestaurantId = "rest-" + System.currentTimeMillis();

        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );
        Address address = Address.create(
                "서울특별시",
                "중구",
                "명동",
                "123번지",
                coordinate
        );

        testOrderer = Orderer.create(
                testUserId,
                "테스트 주문자",
                "010-1234-5678",
                address,
                "문 앞에 놔주세요"
        );

        Restaurant restaurant = Restaurant.create(
                testRestaurantId,
                "테스트 식당",
                "02-1234-5678",
                address
        );

        OrderItem item = OrderItem.create(
                "menu-1",
                "테스트 메뉴",
                new BigDecimal("10000"),
                1,
                restaurant,
                List.of()
        );

        testItems = List.of(item);
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("주문이 정상적으로 생성된다")
        void createOrder_success() {
            // given
            String paymentKey = "test-payment-key";

            // when
            Order result = orderCommandService.createOrder(
                    testOrderer, testItems, paymentKey, testUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
            assertThat(result.getOrderer().getUserId()).isEqualTo(testUserId);
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getPayment().getIsPaid()).isFalse();

            // 이벤트 발행 확인
            verify(orderEventPublisher).publishPaymentRequested(any(Order.class), anyString());
        }

        @Test
        @DisplayName("주문 생성 후 DB에서 조회 가능하다")
        void createOrder_canBeRetrieved() {
            // given
            String paymentKey = "test-payment-key";

            // when
            Order createdOrder = orderCommandService.createOrder(
                    testOrderer, testItems, paymentKey, testUserId);

            // then
            Order foundOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
            assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());
            assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }
    }

    @Nested
    @DisplayName("결제 완료 처리")
    class CompletePayment {

        @Test
        @DisplayName("결제가 정상적으로 완료된다")
        void completePayment_success() {
            // given
            Order order = createTestOrder();
            String paymentId = "payment-123";
            LocalDateTime paymentCompletedAt = LocalDateTime.now();

            // when
            orderCommandService.completePayment(
                    order.getId(), paymentId, paymentCompletedAt, testUserId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(updatedOrder.getPayment().isCompleted()).isTrue();
            assertThat(updatedOrder.getPaymentCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("다른 사용자의 결제 완료 시 예외가 발생한다")
        void completePayment_otherUser_throwsException() {
            // given
            Order order = createTestOrder();
            String otherUserId = "other-user";

            // when & then
            assertThatThrownBy(() -> orderCommandService.completePayment(
                    order.getId(), "payment-123", LocalDateTime.now(), otherUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("이미 결제 완료된 주문은 다시 처리되지 않는다")
        void completePayment_alreadyCompleted_noChange() {
            // given
            Order order = createTestOrder();
            LocalDateTime firstPaymentTime = LocalDateTime.now();
            orderCommandService.completePayment(
                    order.getId(), "payment-123", firstPaymentTime, testUserId);

            // when
            orderCommandService.completePayment(
                    order.getId(), "payment-456", LocalDateTime.now(), testUserId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class ChangeOrderStatus {

        @Test
        @DisplayName("가게 확인이 정상적으로 처리된다")
        void confirmOrder_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);

            // when
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(updatedOrder.getConfirmedAt()).isNotNull();
        }

        @Test
        @DisplayName("다른 레스토랑의 주문 확인 시 예외가 발생한다")
        void confirmOrder_wrongRestaurant_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String wrongRestaurantId = "wrong-restaurant";

            // when & then
            assertThatThrownBy(() -> orderCommandService.confirmOrder(
                    order.getId(), wrongRestaurantId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("조리 시작이 정상적으로 처리된다")
        void startPreparing_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);

            // when
            orderCommandService.startPreparing(order.getId(), testRestaurantId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("배달 시작이 정상적으로 처리된다")
        void startDelivering_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);

            // when
            orderCommandService.startDelivering(order.getId(), testRestaurantId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERING);
        }

        @Test
        @DisplayName("주문 완료가 정상적으로 처리된다")
        void completeOrder_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);
            orderCommandService.startDelivering(order.getId(), testRestaurantId);

            // when
            orderCommandService.completeOrder(order.getId(), testUserId);

            // then
            Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(updatedOrder.getCompletedAt()).isNotNull();
        }
    }

    // Helper methods
    private Order createTestOrder() {
        Payment payment = Payment.createPending("PAY-TEMP-" + System.nanoTime());
        Order order = Order.create(
                testOrderer,
                testItems,
                payment,
                LocalDateTime.now(),
                testUserId
        );
        return orderRepository.save(order);
    }

    private void completePaymentAndSetPending(Order order) {
        order.completePayment("payment-123", LocalDateTime.now(), "SYSTEM");
        order.toPending("SYSTEM");
        orderRepository.save(order);
    }
}