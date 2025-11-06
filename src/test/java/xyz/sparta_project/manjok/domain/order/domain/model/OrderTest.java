package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Aggregate Root 테스트")
class OrderTest {

    private Orderer orderer;
    private Restaurant restaurant;
    private OrderItem item;
    private Payment payment;

    @BeforeEach
    void setup() {
        Address address = Address.create("서울특별시", "강남구", "역삼동", "101-1",
                Coordinate.create(BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.03)));

        orderer = Orderer.create("userA", "홍길동", "01012345678", address, "문 앞에 두세요");

        restaurant = Restaurant.create("R001", "홍콩반점", "02-111-2222", address);

        OrderOption extraSauce = OrderOption.create("추가 소스", "소스 추가", BigDecimal.valueOf(500), 1);
        OrderOptionGroup group = OrderOptionGroup.create("추가 옵션", List.of(extraSauce));

        item = OrderItem.create("M001", "짜장면", BigDecimal.valueOf(6000), 2, restaurant, List.of(group));

        payment = Payment.createPending("P001");
    }

    @Test
    @DisplayName("주문 생성 시 총 가격과 상태가 올바르게 설정된다")
    void create_success() {
        // when
        Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(order.getTotalPrice()).isGreaterThan(BigDecimal.ZERO);
        assertThat(order.getItems()).hasSize(1);
    }

    @Nested
    @DisplayName("결제 완료 처리")
    class CompletePayment {

        @Test
        @DisplayName("PAYMENT_PENDING 상태에서만 결제 완료 가능하다")
        void completePayment_success() {
            Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");

            LocalDateTime completedTime = LocalDateTime.now();
            order.completePayment("P001", completedTime, "system");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            assertThat(order.getPayment().getIsPaid()).isTrue();
            assertThat(order.getPaymentCompletedAt()).isEqualTo(completedTime);
        }

        @Test
        @DisplayName("결제 대기 상태가 아니면 결제 완료 불가")
        void completePayment_fail() {
            Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");
            order.completePayment("P001", LocalDateTime.now(), "system");

            assertThatThrownBy(() ->
                    order.completePayment("P001", LocalDateTime.now(), "system"))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Test
    @DisplayName("주문 절차 상태 전이 전체 흐름 테스트")
    void fullOrderFlow() {
        Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");

        order.completePayment("P001", LocalDateTime.now(), "system");
        order.toPending("system");
        order.confirm(LocalDateTime.now(), "system");
        order.startPreparing("system");
        order.startDelivering("system");
        order.complete(LocalDateTime.now(), "system");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrderTest {

        @Test
        @DisplayName("결제 완료 후 5분 이내이면 취소 가능하다")
        void cancel_within5Minutes_success() {
            Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");
            order.completePayment("P001", LocalDateTime.now(), "system");

            order.cancel("단순 변심", LocalDateTime.now().plusMinutes(4), "system");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(order.getCancelReason()).isEqualTo("단순 변심");
        }

        @Test
        @DisplayName("결제 완료 후 5분이 지나면 취소 불가")
        void cancel_after5Minutes_fail() {
            Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");
            order.completePayment("P001", LocalDateTime.now(), "system");

            assertThatThrownBy(() ->
                    order.cancel("늦었음", LocalDateTime.now().plusMinutes(6), "system"))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("배달 중 상태에서는 취소 불가")
        void cancel_fail_delivering() {
            Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");
            order.completePayment("P001", LocalDateTime.now(), "system");
            order.toPending("system");
            order.confirm(LocalDateTime.now(), "system");
            order.startPreparing("system");
            order.startDelivering("system");

            assertThatThrownBy(() ->
                    order.cancel("변심", LocalDateTime.now(), "system"))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Test
    @DisplayName("완료 또는 취소된 주문만 softDelete 가능")
    void softDelete() {
        Order order = Order.create(orderer, List.of(item), payment, LocalDateTime.now(), "system");
        order.completePayment("P001", LocalDateTime.now(), "system");
        order.toPending("system");
        order.confirm(LocalDateTime.now(), "system");
        order.startPreparing("system");
        order.startDelivering("system");
        order.complete(LocalDateTime.now(), "system");

        order.softDelete("system", LocalDateTime.now());

        assertThat(order.getIsDeleted()).isTrue();
    }
}
