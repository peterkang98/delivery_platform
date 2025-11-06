package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Restaurant Value Object 테스트")
class RestaurantTest {

    private Address address;

    @BeforeEach
    void setUp() {
        // given - 모든 테스트에서 사용할 기본 Address & Coordinate
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );

        address = Address.create(
                "서울특별시",
                "강남구",
                "역삼동",
                "테헤란로 123",
                coordinate
        );
    }

    @Nested
    @DisplayName("create() - 레스토랑 생성")
    class CreateRestaurant {

        @Test
        @DisplayName("정상적인 값으로 Restaurant 생성에 성공한다")
        void create_success() {
            // when
            Restaurant restaurant = Restaurant.create(
                    "R001",
                    "홍콩반점",
                    "02-1234-5678",
                    address
            );

            // then
            assertThat(restaurant.getRestaurantId()).isEqualTo("R001");
            assertThat(restaurant.getRestaurantName()).isEqualTo("홍콩반점");
            assertThat(restaurant.getPhone()).isEqualTo("02-1234-5678");
            assertThat(restaurant.getAddress()).isEqualTo(address);
        }

        @Test
        @DisplayName("restaurantId 가 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidRestaurantId() {
            assertThatThrownBy(() ->
                    Restaurant.create(null, "홍콩반점", "02-1234-5678", address)
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Restaurant.create(" ", "홍콩반점", "02-1234-5678", address)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("restaurantName 이 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidName() {
            assertThatThrownBy(() ->
                    Restaurant.create("R001", "", "02-1234-5678", address)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("전화번호가 null 또는 공백이면 예외가 발생한다")
        void create_fail_invalidPhone() {
            assertThatThrownBy(() ->
                    Restaurant.create("R001", "홍콩반점", " ", address)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("주소가 null 이면 예외가 발생한다")
        void create_fail_addressNull() {
            assertThatThrownBy(() ->
                    Restaurant.create("R001", "홍콩반점", "02-1234-5678", null)
            ).isInstanceOf(OrderException.class);
        }
    }
}
