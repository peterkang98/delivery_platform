package xyz.sparta_project.manjok.domain.order.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;
import xyz.sparta_project.manjok.domain.order.domain.model.*;
import xyz.sparta_project.manjok.domain.order.domain.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderQueryService 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderQueryService 통합 테스트")
class OrderQueryServiceTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepository orderRepository;

    private String testUserId;
    private String testRestaurantId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUserId = "user-" + System.currentTimeMillis();
        testRestaurantId = "rest-" + System.currentTimeMillis();
        testOrder = createAndSaveTestOrder(testUserId, testRestaurantId);
    }

    @Nested
    @DisplayName("주문 단건 조회")
    class GetOrder {

        @Test
        @DisplayName("본인의 주문을 정상적으로 조회한다")
        void getOrder_ownOrder_success() {
            // when
            Order result = orderQueryService.getOrder(testOrder.getId(), testUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testOrder.getId());
            assertThat(result.getOrderer().getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("다른 사용자의 주문 조회 시 예외가 발생한다")
        void getOrder_otherUserOrder_throwsException() {
            // given
            String otherUserId = "other-user";

            // when & then
            assertThatThrownBy(() -> orderQueryService.getOrder(testOrder.getId(), otherUserId))
                    .isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 예외가 발생한다")
        void getOrder_notExists_throwsException() {
            // when & then
            assertThatThrownBy(() -> orderQueryService.getOrder("non-existent-id", testUserId))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("사용자 주문 목록 조회")
    class GetUserOrders {

        @Test
        @DisplayName("사용자의 주문 목록을 조회한다")
        void getUserOrders_success() {
            // given
            createAndSaveTestOrder(testUserId, testRestaurantId);
            createAndSaveTestOrder(testUserId, testRestaurantId);
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderQueryService.getUserOrders(testUserId, pageRequest);

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
            assertThat(result.getContent())
                    .allMatch(order -> order.getOrderer().getUserId().equals(testUserId));
        }

        @Test
        @DisplayName("페이징이 정상적으로 동작한다")
        void getUserOrders_paging_works() {
            // given
            createAndSaveTestOrder(testUserId, testRestaurantId);
            createAndSaveTestOrder(testUserId, testRestaurantId);
            PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Order> result = orderQueryService.getUserOrders(testUserId, pageRequest);

            // then
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("상태로 필터링하여 조회한다")
        void getUserOrdersByStatus_filters() {
            // given
            Order order = createAndSaveTestOrder(testUserId, testRestaurantId);
            order.completePayment("PAY-123", LocalDateTime.now(), testUserId);
            orderRepository.save(order);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderQueryService.getUserOrdersByStatus(
                    testUserId, OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderQueryService.getUserOrdersByStatus(
                    testUserId, OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).isNotEmpty();
            assertThat(pendingOrders.getContent())
                    .allMatch(o -> o.getStatus() == OrderStatus.PAYMENT_PENDING);

            assertThat(completedOrders.getContent()).isNotEmpty();
            assertThat(completedOrders.getContent())
                    .allMatch(o -> o.getStatus() == OrderStatus.PAYMENT_COMPLETED);
        }
    }

    @Nested
    @DisplayName("레스토랑 주문 목록 조회")
    class GetRestaurantOrders {

        @Test
        @DisplayName("레스토랑의 주문 목록을 조회한다")
        void getRestaurantOrders_success() {
            // given
            createAndSaveTestOrder("user-1", testRestaurantId);
            createAndSaveTestOrder("user-2", testRestaurantId);
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderQueryService.getRestaurantOrders(testRestaurantId, pageRequest);

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(3);
            assertThat(result.getContent()).allMatch(order ->
                    order.getItems().get(0).getRestaurantId().equals(testRestaurantId));
        }

        @Test
        @DisplayName("상태로 필터링하여 조회한다")
        void getRestaurantOrdersByStatus_filters() {
            // given
            Order order = createAndSaveTestOrder("user-1", testRestaurantId);
            order.completePayment("PAY-123", LocalDateTime.now(), "user-1");
            orderRepository.save(order);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderQueryService.getRestaurantOrdersByStatus(
                    testRestaurantId, OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderQueryService.getRestaurantOrdersByStatus(
                    testRestaurantId, OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).isNotEmpty();
            assertThat(completedOrders.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("기간으로 필터링하여 조회한다")
        void getRestaurantOrdersByDateRange_filters() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderQueryService.getRestaurantOrdersByDateRange(
                    testRestaurantId, startDate, endDate, pageRequest);

            // then
            assertThat(result.getContent()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("전체 주문 조회 (관리자)")
    class GetAllOrders {

        @Test
        @DisplayName("전체 주문 목록을 조회한다")
        void getAllOrders_success() {
            // given
            createAndSaveTestOrder("user-1", "rest-1");
            createAndSaveTestOrder("user-2", "rest-2");
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderQueryService.getAllOrders(pageRequest);

            // then
            assertThat(result.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("상태로 필터링하여 조회한다")
        void getAllOrdersByStatus_filters() {
            // given
            Order order = createAndSaveTestOrder("user-1", "rest-1");
            order.completePayment("PAY-123", LocalDateTime.now(), "user-1");
            orderRepository.save(order);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderQueryService.getAllOrdersByStatus(
                    OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderQueryService.getAllOrdersByStatus(
                    OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).isNotEmpty();
            assertThat(completedOrders.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("기간으로 필터링하여 조회한다")
        void getOrdersByDateRange_filters() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderQueryService.getOrdersByDateRange(
                    startDate, endDate, pageRequest);

            // then
            assertThat(result.getContent()).isNotEmpty();
        }
    }

    // Helper method
    private Order createAndSaveTestOrder(String userId, String restaurantId) {
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
        Orderer orderer = Orderer.create(
                userId,
                "테스트 주문자",
                "010-1234-5678",
                address,
                "문 앞에 놔주세요"
        );

        Restaurant restaurant = Restaurant.create(
                restaurantId,
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

        Payment payment = Payment.createPending("PAY-TEMP-" + System.nanoTime());

        Order order = Order.create(
                orderer,
                List.of(item),
                payment,
                LocalDateTime.now(),
                userId
        );

        return orderRepository.save(order);
    }
}