package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem 도메인 테스트")
class OrderItemTest {

    private Coordinate createCoord() {
        return Coordinate.create(
                BigDecimal.valueOf(37.498095),
                BigDecimal.valueOf(127.027610)
        );
    }

    private Address createAddress() {
        return Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "테헤란로 123",
                createCoord()
        );
    }

    private Restaurant createRestaurant() {
        return Restaurant.create(
                "R-100",
                "홍콩반점",
                "02-111-2222",
                createAddress()
        );
    }

    private OrderOption createOption(String name, BigDecimal price, int qty) {
        return OrderOption.create(
                name,
                "맛있습니다.",
                price,
                qty
        );
    }

    private OrderOptionGroup createOptionGroup() {
        return OrderOptionGroup.create(
                "곱빼기 / 추가 옵션",
                List.of(
                        createOption("곱빼기", BigDecimal.valueOf(2000), 1),
                        createOption("고기추가", BigDecimal.valueOf(3000), 2)
                )
        );
    }

    @Nested
    @DisplayName("create() 주문 아이템 생성")
    class CreateOrderItem {

        @Test
        @DisplayName("정상 생성 시 총 가격 계산이 올바르게 이루어진다")
        void create_success_withOptions() {
            // given
            Restaurant restaurant = createRestaurant();
            OrderOptionGroup group = createOptionGroup();

            // when
            OrderItem item = OrderItem.create(
                    "M-10",
                    "짜장면",
                    BigDecimal.valueOf(6000),
                    2,
                    restaurant,
                    List.of(group)
            );

            // then
            // 옵션 총합 = (2000*1 + 3000*2) = 8000
            // 기본가 + 옵션 = (6000 + 8000) = 14000
            // 수량 2 → 28000
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(28000));
            assertThat(item.getOptionGroups()).hasSize(1);
            assertThat(item.getRestaurant()).isEqualTo(restaurant);
        }

        @Test
        @DisplayName("옵션 그룹이 없어도 정상 생성된다")
        void create_success_noOptions() {
            // given
            Restaurant restaurant = createRestaurant();

            // when
            OrderItem item = OrderItem.create(
                    "M-11",
                    "짬뽕",
                    BigDecimal.valueOf(7000),
                    1,
                    restaurant,
                    null
            );

            // then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(7000));
            assertThat(item.getOptionGroups()).isEmpty();
        }

        @Test
        @DisplayName("menuId가 null 또는 공백이면 예외 발생")
        void fail_invalidMenuId() {
            Restaurant restaurant = createRestaurant();

            assertThatThrownBy(() ->
                    OrderItem.create(null, "짜장면", BigDecimal.valueOf(6000), 1, restaurant, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("메뉴 이름이 null 또는 공백이면 예외 발생")
        void fail_invalidMenuName() {
            Restaurant restaurant = createRestaurant();

            assertThatThrownBy(() ->
                    OrderItem.create("M-10", " ", BigDecimal.valueOf(6000), 1, restaurant, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("기본 가격이 0 이하이면 예외 발생")
        void fail_invalidPrice() {
            Restaurant restaurant = createRestaurant();

            assertThatThrownBy(() ->
                    OrderItem.create("M-10", "짜장면", BigDecimal.ZERO, 1, restaurant, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("수량이 1 미만이면 예외 발생")
        void fail_invalidQuantity() {
            Restaurant restaurant = createRestaurant();

            assertThatThrownBy(() ->
                    OrderItem.create("M-10", "짜장면", BigDecimal.valueOf(6000), 0, restaurant, null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("레스토랑 정보가 null이면 예외 발생")
        void fail_nullRestaurant() {
            assertThatThrownBy(() ->
                    OrderItem.create("M-10", "짜장면", BigDecimal.valueOf(6000), 1, null, null)
            ).isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("옵션 그룹 추가 기능")
    class AddOptionGroup {

        @Test
        @DisplayName("옵션 그룹 추가 시 총 가격이 다시 계산된다")
        void addOptionGroup_recalculatesTotal() {
            // given
            Restaurant restaurant = createRestaurant();
            OrderItem item = OrderItem.create(
                    "M-10",
                    "짜장면",
                    BigDecimal.valueOf(6000),
                    1,
                    restaurant,
                    null
            );

            OrderOptionGroup group = createOptionGroup();

            // when
            item.addOptionGroup(group);

            // then
            assertThat(item.getTotalPrice()).isEqualTo(
                    BigDecimal.valueOf(6000 + 8000)
            );
        }
    }
}
