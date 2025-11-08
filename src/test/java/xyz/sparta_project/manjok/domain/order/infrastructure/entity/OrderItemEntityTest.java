package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItemEntity 변환 테스트")
class OrderItemEntityTest {

    @Test
    @DisplayName("도메인 -> 엔티티 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Coordinate coordinate = Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("126.9780"));
        Address address = Address.create("서울특별시", "중구", "명동", "123번지", coordinate);
        Restaurant restaurant = Restaurant.create("rest-1", "맛있는 식당", "02-1234-5678", address);

        OrderOption option = OrderOption.create("치즈 추가", "고소한 치즈", new BigDecimal("1000"), 1);
        OrderOptionGroup optionGroup = OrderOptionGroup.create("토핑", List.of(option));

        OrderItem domain = OrderItem.create(
                "menu-1",
                "버거",
                new BigDecimal("5000"),
                2,
                restaurant,
                List.of(optionGroup)
        );

        OrderEntity mockOrder = OrderEntity.builder().build();

        // when
        OrderItemEntity entity = OrderItemEntity.from(domain, mockOrder);

        // then
        assertThat(entity.getOrderItemNumber()).isNotBlank();
        assertThat(entity.getMenuId()).isEqualTo("menu-1");
        assertThat(entity.getMenuName()).isEqualTo("버거");
        assertThat(entity.getBasePrice()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(entity.getQuantity()).isEqualTo(2);
        assertThat(entity.getOptionGroups()).hasSize(1);
        assertThat(entity.getOrder()).isEqualTo(mockOrder);
    }

    @Test
    @DisplayName("엔티티 -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        RestaurantVO restaurantVO = RestaurantVO.builder()
                .restaurantId("rest-2")
                .restaurantName("훌륭한 레스토랑")
                .phone("02-9876-5432")
                .province("서울특별시")
                .city("강남구")
                .district("역삼동")
                .detailAddress("456번지")
                .latitude(new BigDecimal("37.5000"))
                .longitude(new BigDecimal("127.0000"))
                .build();

        OrderItemEntity entity = OrderItemEntity.builder()
                .orderItemNumber("OI-123")
                .menuId("menu-2")
                .menuName("피자")
                .basePrice(new BigDecimal("15000"))
                .quantity(1)
                .totalPrice(new BigDecimal("15000"))
                .restaurant(restaurantVO)
                .build();

        // when
        OrderItem domain = entity.toDomain();

        // then
        assertThat(domain.getOrderItemNumber()).isEqualTo("OI-123");
        assertThat(domain.getMenuId()).isEqualTo("menu-2");
        assertThat(domain.getMenuName()).isEqualTo("피자");
        assertThat(domain.getBasePrice()).isEqualByComparingTo(new BigDecimal("15000"));
        assertThat(domain.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("엔티티에 옵션 그룹 추가 시 정상적으로 동작한다")
    void addOptionGroup_success() {
        // given
        OrderItemEntity entity = OrderItemEntity.builder()
                .orderItemNumber("OI-456")
                .menuId("menu-3")
                .menuName("샐러드")
                .basePrice(new BigDecimal("8000"))
                .quantity(1)
                .totalPrice(new BigDecimal("8000"))
                .build();

        OrderOptionGroupEntity optionGroupEntity = OrderOptionGroupEntity.builder()
                .groupName("드레싱")
                .groupTotalPrice(new BigDecimal("500"))
                .build();

        // when
        entity.addOptionGroup(optionGroupEntity);

        // then
        assertThat(entity.getOptionGroups()).hasSize(1);
        assertThat(optionGroupEntity.getOrderItem()).isEqualTo(entity);
    }

    @Test
    @DisplayName("엔티티에서 옵션 그룹 제거 시 정상적으로 동작한다")
    void removeOptionGroup_success() {
        // given
        OrderItemEntity entity = OrderItemEntity.builder()
                .orderItemNumber("OI-789")
                .menuId("menu-4")
                .menuName("파스타")
                .basePrice(new BigDecimal("12000"))
                .quantity(1)
                .totalPrice(new BigDecimal("12000"))
                .build();

        OrderOptionGroupEntity optionGroupEntity = OrderOptionGroupEntity.builder()
                .groupName("토핑")
                .groupTotalPrice(new BigDecimal("1000"))
                .build();

        entity.addOptionGroup(optionGroupEntity);

        // when
        entity.removeOptionGroup(optionGroupEntity);

        // then
        assertThat(entity.getOptionGroups()).isEmpty();
        assertThat(optionGroupEntity.getOrderItem()).isNull();
    }
}