package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderEntity 변환 테스트")
class OrderEntityTest {

    @Test
    @DisplayName("도메인 -> 엔티티 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Coordinate coordinate = Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("126.9780"));
        Address address = Address.create("서울특별시", "중구", "명동", "123번지", coordinate);
        Orderer orderer = Orderer.create("user-1", "홍길동", "010-1234-5678", address, "문 앞에 놔주세요");

        Restaurant restaurant = Restaurant.create("rest-1", "맛있는 식당", "02-1234-5678", address);
        OrderItem item = OrderItem.create("menu-1", "버거", new BigDecimal("5000"), 1, restaurant, List.of());

        Payment payment = Payment.create("PAY-123", false);
        LocalDateTime now = LocalDateTime.now();

        Order domain = Order.create(orderer, List.of(item), payment, now, "user-1");

        // when
        OrderEntity entity = OrderEntity.from(domain);

        // then
        assertThat(entity.getOrderer()).isNotNull();
        assertThat(entity.getItems()).hasSize(1);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(entity.getPayment()).isNotNull();
        assertThat(entity.getTotalPrice()).isPositive();
        assertThat(entity.getRequestedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("엔티티 -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        CoordinateVO coordinateVO = CoordinateVO.builder()
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .build();
        AddressVO addressVO = AddressVO.builder()
                .province("서울특별시")
                .city("중구")
                .district("명동")
                .detailAddress("123번지")
                .coordinate(coordinateVO)
                .build();
        OrdererVO ordererVO = OrdererVO.builder()
                .userId("user-2")
                .name("김철수")
                .phone("010-9876-5432")
                .address(addressVO)
                .build();
        PaymentVO paymentVO = PaymentVO.builder()
                .paymentId("PAY-456")
                .isPaid(true)
                .build();

        OrderEntity entity = OrderEntity.builder()
                .orderer(ordererVO)
                .status(OrderStatus.CONFIRMED)
                .payment(paymentVO)
                .totalPrice(new BigDecimal("10000"))
                .requestedAt(LocalDateTime.now())
                .createdBy("user-2")
                .isDeleted(false)
                .build();

        // when
        Order domain = entity.toDomain();

        // then
        assertThat(domain.getOrderer()).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(domain.getPayment()).isNotNull();
        assertThat(domain.getTotalPrice()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("엔티티에 OrderItem 추가 시 정상적으로 동작한다")
    void addItem_success() {
        // given
        OrderEntity entity = OrderEntity.builder()
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("5000"))
                .isDeleted(false)
                .build();

        RestaurantVO restaurantVO = RestaurantVO.builder()
                .restaurantId("rest-1")
                .restaurantName("식당")
                .phone("02-1111-2222")
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("100번지")
                .latitude(new BigDecimal("37.5000"))
                .longitude(new BigDecimal("127.0000"))
                .build();

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .orderItemNumber("OI-123")
                .menuId("menu-1")
                .menuName("피자")
                .basePrice(new BigDecimal("15000"))
                .quantity(1)
                .totalPrice(new BigDecimal("15000"))
                .restaurant(restaurantVO)
                .build();

        // when
        entity.addItem(itemEntity);

        // then
        assertThat(entity.getItems()).hasSize(1);
        assertThat(itemEntity.getOrder()).isEqualTo(entity);
    }

    @Test
    @DisplayName("엔티티에서 OrderItem 제거 시 정상적으로 동작한다")
    void removeItem_success() {
        // given
        OrderEntity entity = OrderEntity.builder()
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("5000"))
                .isDeleted(false)
                .build();

        RestaurantVO restaurantVO = RestaurantVO.builder()
                .restaurantId("rest-2")
                .restaurantName("식당2")
                .phone("02-3333-4444")
                .province("서울특별시")
                .city("송파구")
                .district("잠실동")
                .detailAddress("200번지")
                .latitude(new BigDecimal("37.5000"))
                .longitude(new BigDecimal("127.0000"))
                .build();

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .orderItemNumber("OI-456")
                .menuId("menu-2")
                .menuName("샐러드")
                .basePrice(new BigDecimal("8000"))
                .quantity(1)
                .totalPrice(new BigDecimal("8000"))
                .restaurant(restaurantVO)
                .build();

        entity.addItem(itemEntity);

        // when
        entity.removeItem(itemEntity);

        // then
        assertThat(entity.getItems()).isEmpty();
        assertThat(itemEntity.getOrder()).isNull();
    }

    @Test
    @DisplayName("소프트 삭제 시 관련 필드가 업데이트된다")
    void markAsDeleted_success() {
        // given
        OrderEntity entity = OrderEntity.builder()
                .status(OrderStatus.COMPLETED)
                .isDeleted(false)
                .build();

        LocalDateTime now = LocalDateTime.now();

        // when
        entity.markAsDeleted("admin-1", now, now, "admin-1");

        // then
        assertThat(entity.getIsDeleted()).isTrue();
        assertThat(entity.getDeletedBy()).isEqualTo("admin-1");
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }
}