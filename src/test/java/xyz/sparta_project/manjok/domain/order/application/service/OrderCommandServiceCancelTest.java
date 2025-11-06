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
 * OrderCommandService 통합 테스트 - 취소 및 삭제
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderCommandService 통합 테스트 - 취소 및 삭제")
class OrderCommandServiceCancelTest {

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
    @DisplayName("주문 취소 요청")
    class CancelOrder {

        @Test
        @DisplayName("주문 취소 요청이 정상적으로 처리된다")
        void cancelOrder_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String cancelReason = "단순 변심";

            // when
            orderCommandService.cancelOrder(order.getId(), cancelReason, testUserId);

            // then
            verify(orderEventPublisher).publishOrderCancelRequested(
                    any(Order.class), anyString(), anyString());
        }

        @Test
        @DisplayName("다른 사용자의 주문 취소 시 예외가 발생한다")
        void cancelOrder_otherUser_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String otherUserId = "other-user";

            // when & then
            assertThatThrownBy(() -> orderCommandService.cancelOrder(
                    order.getId(), "취소 사유", otherUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("배달 중인 주문 취소 시 예외가 발생한다")
        void cancelOrder_delivering_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);
            orderCommandService.startDelivering(order.getId(), testRestaurantId);

            // when & then
            assertThatThrownBy(() -> orderCommandService.cancelOrder(
                    order.getId(), "취소 사유", testUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("완료된 주문 취소 시 예외가 발생한다")
        void cancelOrder_completed_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);
            orderCommandService.startDelivering(order.getId(), testRestaurantId);
            orderCommandService.completeOrder(order.getId(), testUserId);

            // when & then
            assertThatThrownBy(() -> orderCommandService.cancelOrder(
                    order.getId(), "취소 사유", testUserId))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소 완료 처리")
    class CompleteOrderCancellation {

        @Test
        @DisplayName("주문 취소가 정상적으로 완료된다")
        void completeOrderCancellation_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String paymentId = order.getPayment().getPaymentId();
            String cancelReason = "단순 변심";

            // when
            orderCommandService.completeOrderCancellation(
                    order.getId(), paymentId, cancelReason, testUserId);

            // then
            Order canceledOrder = orderRepository.findByIdIncludingDeleted(order.getId())
                    .orElseThrow();
            assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(canceledOrder.getCancelReason()).isEqualTo(cancelReason);
            assertThat(canceledOrder.getCanceledAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 정보가 일치하지 않으면 예외가 발생한다")
        void completeOrderCancellation_wrongPaymentId_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String wrongPaymentId = "wrong-payment-id";

            // when & then
            assertThatThrownBy(() -> orderCommandService.completeOrderCancellation(
                    order.getId(), wrongPaymentId, "취소 사유", testUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("취소 불가능한 상태에서 취소 완료 시 예외가 발생한다")
        void completeOrderCancellation_notCancelable_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);
            orderCommandService.startDelivering(order.getId(), testRestaurantId);

            String paymentId = order.getPayment().getPaymentId();

            // when & then
            assertThatThrownBy(() -> orderCommandService.completeOrderCancellation(
                    order.getId(), paymentId, "취소 사유", testUserId))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("주문 삭제")
    class DeleteOrder {

        @Test
        @DisplayName("취소된 주문이 정상적으로 삭제된다")
        void deleteOrder_canceled_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String paymentId = order.getPayment().getPaymentId();
            orderCommandService.completeOrderCancellation(
                    order.getId(), paymentId, "취소 사유", testUserId);

            // when
            orderCommandService.deleteOrder(order.getId(), testUserId);

            // then
            Order deletedOrder = orderRepository.findByIdIncludingDeleted(order.getId())
                    .orElseThrow();
            assertThat(deletedOrder.getIsDeleted()).isTrue();
            assertThat(deletedOrder.getDeletedBy()).isEqualTo(testUserId);
            assertThat(deletedOrder.getDeletedAt()).isNotNull();

            // 일반 조회에서는 조회되지 않음
            assertThat(orderRepository.findById(order.getId())).isEmpty();
        }

        @Test
        @DisplayName("완료된 주문이 정상적으로 삭제된다")
        void deleteOrder_completed_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);
            orderCommandService.startPreparing(order.getId(), testRestaurantId);
            orderCommandService.startDelivering(order.getId(), testRestaurantId);
            orderCommandService.completeOrder(order.getId(), testUserId);

            // when
            orderCommandService.deleteOrder(order.getId(), testUserId);

            // then
            Order deletedOrder = orderRepository.findByIdIncludingDeleted(order.getId())
                    .orElseThrow();
            assertThat(deletedOrder.getIsDeleted()).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 주문 삭제 시 예외가 발생한다")
        void deleteOrder_otherUser_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String paymentId = order.getPayment().getPaymentId();
            orderCommandService.completeOrderCancellation(
                    order.getId(), paymentId, "취소 사유", testUserId);
            String otherUserId = "other-user";

            // when & then
            assertThatThrownBy(() -> orderCommandService.deleteOrder(order.getId(), otherUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("진행 중인 주문 삭제 시 예외가 발생한다")
        void deleteOrder_inProgress_throwsException() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            orderCommandService.confirmOrder(order.getId(), testRestaurantId);

            // when & then
            assertThatThrownBy(() -> orderCommandService.deleteOrder(order.getId(), testUserId))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("환불 실패 처리")
    class HandleRefundFailure {

        @Test
        @DisplayName("환불 실패 처리가 정상적으로 수행된다")
        void handleRefundFailure_success() {
            // given
            Order order = createTestOrder();
            completePaymentAndSetPending(order);
            String failureReason = "결제사 오류";

            // when & then (예외 발생하지 않음)
            assertThatNoException().isThrownBy(() ->
                    orderCommandService.handleRefundFailure(
                            order.getId(), failureReason, testUserId));
        }

        @Test
        @DisplayName("존재하지 않는 주문의 환불 실패 처리 시 예외가 발생한다")
        void handleRefundFailure_notExists_throwsException() {
            // when & then
            assertThatThrownBy(() -> orderCommandService.handleRefundFailure(
                    "non-existent-id", "실패 사유", testUserId))
                    .isInstanceOf(OrderException.class);
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