package xyz.sparta_project.manjok.domain.order.domain.repository;

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
import xyz.sparta_project.manjok.domain.order.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderRepository 추상체 기준 통합 테스트
 * - 구현체(JPA, MyBatis 등)와 무관하게 동작
 * - 인터페이스 명세만 검증
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private String testUserId;
    private String testRestaurantId;

    @BeforeEach
    void setUp() {
        testUserId = "user-" + System.currentTimeMillis();
        testRestaurantId = "rest-" + System.currentTimeMillis();
    }

    @Nested
    @DisplayName("저장 및 수정")
    class SaveAndUpdate {

        @Test
        @DisplayName("신규 주문이 정상적으로 저장된다")
        void save_newOrder_success() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);

            // when
            Order savedOrder = orderRepository.save(order);

            // then
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(savedOrder.getOrderer().getUserId()).isEqualTo(testUserId);
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
            assertThat(savedOrder.getItems()).hasSize(1);
            assertThat(savedOrder.getTotalPrice()).isPositive();
        }

        @Test
        @DisplayName("기존 주문이 정상적으로 업데이트된다")
        void save_updateOrder_success() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);
            String orderId = savedOrder.getId();

            // when
            savedOrder.completePayment("PAY-UPDATED", LocalDateTime.now(), testUserId);
            Order updatedOrder = orderRepository.save(savedOrder);

            // then
            assertThat(updatedOrder.getId()).isEqualTo(orderId);
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            assertThat(updatedOrder.getPayment().isCompleted()).isTrue();
            assertThat(updatedOrder.getPaymentCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ID 조회")
    class FindById {

        @Test
        @DisplayName("존재하는 주문이 정상적으로 조회된다")
        void findById_existing_success() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            // when
            Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
            assertThat(foundOrder.get().getOrderer().getUserId()).isEqualTo(testUserId);
            assertThat(foundOrder.get().getItems()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 주문은 빈 Optional을 반환한다")
        void findById_notExisting_returnsEmpty() {
            // when
            Optional<Order> foundOrder = orderRepository.findById("non-existent-id");

            // then
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("삭제된 주문은 조회되지 않는다")
        void findById_deleted_notFound() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            savedOrder.cancel("테스트 취소", LocalDateTime.now(), testUserId);
            savedOrder.softDelete(testUserId, LocalDateTime.now());
            orderRepository.save(savedOrder);

            // when
            Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

            // then
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("삭제 포함 조회 시 삭제된 주문도 조회된다")
        void findByIdIncludingDeleted_deleted_found() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            savedOrder.cancel("테스트 취소", LocalDateTime.now(), testUserId);
            savedOrder.softDelete(testUserId, LocalDateTime.now());
            orderRepository.save(savedOrder);

            // when
            Optional<Order> foundOrder = orderRepository.findByIdIncludingDeleted(savedOrder.getId());

            // then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getIsDeleted()).isTrue();
            assertThat(foundOrder.get().getDeletedBy()).isEqualTo(testUserId);
        }
    }

    @Nested
    @DisplayName("사용자별 조회")
    class FindByUserId {

        @Test
        @DisplayName("해당 사용자의 주문만 조회된다")
        void findByUserId_filtersByUser() {
            // given
            String otherUserId = "other-user-" + System.currentTimeMillis();

            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder(otherUserId, testRestaurantId));

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.findByUserId(testUserId, pageRequest);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .allMatch(order -> order.getOrderer().getUserId().equals(testUserId));
        }

        @Test
        @DisplayName("페이징이 정상적으로 동작한다")
        void findByUserId_paging_works() {
            // given
            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder(testUserId, testRestaurantId));

            PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            Page<Order> result = orderRepository.findByUserId(testUserId, pageRequest);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("사용자+상태별 필터링이 정상적으로 동작한다")
        void findByUserIdAndStatus_filters() {
            // given
            Order order1 = createTestOrder(testUserId, testRestaurantId);
            Order order2 = createTestOrder(testUserId, testRestaurantId);
            Order order3 = createTestOrder(testUserId, testRestaurantId);

            orderRepository.save(order1);

            order2.completePayment("PAY-2", LocalDateTime.now(), testUserId);
            orderRepository.save(order2);

            order3.completePayment("PAY-3", LocalDateTime.now(), testUserId);
            orderRepository.save(order3);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderRepository.findByUserIdAndStatus(
                    testUserId, OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderRepository.findByUserIdAndStatus(
                    testUserId, OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).hasSize(1);
            assertThat(pendingOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_PENDING);

            assertThat(completedOrders.getContent()).hasSize(2);
            assertThat(completedOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_COMPLETED);
        }
    }

    @Nested
    @DisplayName("레스토랑별 조회")
    class FindByRestaurantId {

        @Test
        @DisplayName("해당 레스토랑의 주문만 조회된다")
        void findByRestaurantId_filtersByRestaurant() {
            // given
            String otherRestaurantId = "other-rest-" + System.currentTimeMillis();

            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder("user-2", testRestaurantId));
            orderRepository.save(createTestOrder("user-3", otherRestaurantId));

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.findByRestaurantId(testRestaurantId, pageRequest);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(order -> order.getItems().get(0).getRestaurantId().equals(testRestaurantId));
        }

        @Test
        @DisplayName("레스토랑+상태별 필터링이 정상적으로 동작한다")
        void findByRestaurantIdAndStatus_filters() {
            // given
            Order order1 = createTestOrder(testUserId, testRestaurantId);
            Order order2 = createTestOrder("user-2", testRestaurantId);

            orderRepository.save(order1);

            order2.completePayment("PAY-2", LocalDateTime.now(), "user-2");
            orderRepository.save(order2);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderRepository.findByRestaurantIdAndStatus(
                    testRestaurantId, OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderRepository.findByRestaurantIdAndStatus(
                    testRestaurantId, OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).hasSize(1);
            assertThat(pendingOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_PENDING);

            assertThat(completedOrders.getContent()).hasSize(1);
            assertThat(completedOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_COMPLETED);
        }

        @Test
        @DisplayName("레스토랑+기간별 필터링이 정상적으로 동작한다")
        void findByRestaurantIdAndDateRange_filters() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            orderRepository.save(createTestOrder(testUserId, testRestaurantId));
            orderRepository.save(createTestOrder("user-2", testRestaurantId));

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.findByRestaurantIdAndDateRange(
                    testRestaurantId, startDate, endDate, pageRequest);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(order ->
                    order.getItems().get(0).getRestaurantId().equals(testRestaurantId));
        }
    }

    @Nested
    @DisplayName("전체 조회")
    class FindAll {

        @Test
        @DisplayName("모든 주문이 조회된다")
        void findAll_returnsAll() {
            // given
            orderRepository.save(createTestOrder("user-1", "rest-1"));
            orderRepository.save(createTestOrder("user-2", "rest-2"));
            orderRepository.save(createTestOrder("user-3", "rest-3"));

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.findAll(pageRequest);

            // then
            assertThat(result.getContent().size()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("페이징이 정상적으로 동작한다")
        void findAll_paging_works() {
            // given
            orderRepository.save(createTestOrder("user-1", "rest-1"));
            orderRepository.save(createTestOrder("user-2", "rest-2"));
            orderRepository.save(createTestOrder("user-3", "rest-3"));

            PageRequest pageRequest = PageRequest.of(0, 2);

            // when
            Page<Order> result = orderRepository.findAll(pageRequest);

            // then
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(2);
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("상태별 필터링이 정상적으로 동작한다")
        void findAllByStatus_filters() {
            // given
            Order order1 = createTestOrder("user-1", "rest-1");
            Order order2 = createTestOrder("user-2", "rest-2");

            orderRepository.save(order1);

            order2.completePayment("PAY-2", LocalDateTime.now(), "user-2");
            orderRepository.save(order2);

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> pendingOrders = orderRepository.findAllByStatus(
                    OrderStatus.PAYMENT_PENDING, pageRequest);
            Page<Order> completedOrders = orderRepository.findAllByStatus(
                    OrderStatus.PAYMENT_COMPLETED, pageRequest);

            // then
            assertThat(pendingOrders.getContent()).isNotEmpty();
            assertThat(pendingOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_PENDING);

            assertThat(completedOrders.getContent()).isNotEmpty();
            assertThat(completedOrders.getContent())
                    .allMatch(order -> order.getStatus() == OrderStatus.PAYMENT_COMPLETED);
        }

        @Test
        @DisplayName("기간별 필터링이 정상적으로 동작한다")
        void findByDateRange_filters() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.minusDays(1);
            LocalDateTime endDate = now.plusDays(1);

            orderRepository.save(createTestOrder("user-1", "rest-1"));
            orderRepository.save(createTestOrder("user-2", "rest-2"));

            PageRequest pageRequest = PageRequest.of(0, 10);

            // when
            Page<Order> result = orderRepository.findByDateRange(startDate, endDate, pageRequest);

            // then
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("특수 조회")
    class SpecialQueries {

        @Test
        @DisplayName("결제 후 일정 시간 지난 미확인 주문이 조회된다")
        void findPendingOrdersAfterPaymentTime_filters() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            LocalDateTime paymentTime = LocalDateTime.now().minusMinutes(10);

            order.completePayment("PAY-123", paymentTime, testUserId);
            order.toPending(testUserId);
            orderRepository.save(order);

            LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(5);

            // when
            List<Order> result = orderRepository.findPendingOrdersAfterPaymentTime(beforeTime);

            // then
            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(o -> o.getStatus() == OrderStatus.PENDING);
            assertThat(result).allMatch(o -> o.getPaymentCompletedAt().isBefore(beforeTime));
        }
    }

    @Nested
    @DisplayName("주문 존재 확인")
    class ExistsById {

        @Test
        @DisplayName("존재하는 주문은 true를 반환한다")
        void existsById_existing_returnsTrue() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            // when
            boolean exists = orderRepository.existsById(savedOrder.getId());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 주문은 false를 반환한다")
        void existsById_notExisting_returnsFalse() {
            // when
            boolean exists = orderRepository.existsById("non-existent-id");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 주문은 false를 반환한다")
        void existsById_deleted_returnsFalse() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            savedOrder.cancel("테스트 취소", LocalDateTime.now(), testUserId);
            savedOrder.softDelete(testUserId, LocalDateTime.now());
            orderRepository.save(savedOrder);

            // when
            boolean exists = orderRepository.existsById(savedOrder.getId());

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("소프트 삭제")
    class Delete {

        @Test
        @DisplayName("삭제 플래그가 설정된다")
        void delete_setsDeleteFlag() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);

            savedOrder.cancel("테스트 취소", LocalDateTime.now(), testUserId);
            savedOrder.softDelete(testUserId, LocalDateTime.now());

            // when
            orderRepository.delete(savedOrder);

            // then
            Order deletedOrder = orderRepository.findByIdIncludingDeleted(savedOrder.getId())
                    .orElseThrow();
            assertThat(deletedOrder.getIsDeleted()).isTrue();
            assertThat(deletedOrder.getDeletedBy()).isEqualTo(testUserId);
            assertThat(deletedOrder.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("삭제 후 일반 조회 시 조회되지 않는다")
        void delete_thenFindById_notFound() {
            // given
            Order order = createTestOrder(testUserId, testRestaurantId);
            Order savedOrder = orderRepository.save(order);
            String orderId = savedOrder.getId();

            savedOrder.cancel("테스트 취소", LocalDateTime.now(), testUserId);
            savedOrder.softDelete(testUserId, LocalDateTime.now());
            orderRepository.delete(savedOrder);

            // when
            Optional<Order> foundOrder = orderRepository.findById(orderId);

            // then
            assertThat(foundOrder).isEmpty();
        }
    }

    // Helper method
    private Order createTestOrder(String userId, String restaurantId) {
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

        return Order.create(
                orderer,
                List.of(item),
                payment,
                LocalDateTime.now(),
                userId
        );
    }
}