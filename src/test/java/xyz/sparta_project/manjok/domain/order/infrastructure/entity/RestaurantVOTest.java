package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.Address;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;
import xyz.sparta_project.manjok.domain.order.domain.model.Restaurant;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RestaurantVO Value Object 변환 테스트")
class RestaurantVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
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
        Restaurant domain = Restaurant.create(
                "rest-123",
                "맛있는 식당",
                "02-1234-5678",
                address
        );

        // when
        RestaurantVO vo = RestaurantVO.from(domain);

        // then
        assertThat(vo.getRestaurantId()).isEqualTo("rest-123");
        assertThat(vo.getRestaurantName()).isEqualTo("맛있는 식당");
        assertThat(vo.getPhone()).isEqualTo("02-1234-5678");
        assertThat(vo.getProvince()).isEqualTo("서울특별시");
        assertThat(vo.getCity()).isEqualTo("중구");
        assertThat(vo.getDistrict()).isEqualTo("명동");
        assertThat(vo.getDetailAddress()).isEqualTo("123번지");
        assertThat(vo.getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665"));
        assertThat(vo.getLongitude()).isEqualByComparingTo(new BigDecimal("126.9780"));
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        RestaurantVO vo = RestaurantVO.builder()
                .restaurantId("rest-456")
                .restaurantName("훌륭한 레스토랑")
                .phone("02-9876-5432")
                .province("부산광역시")
                .city("해운대구")
                .district("우동")
                .detailAddress("456번지")
                .latitude(new BigDecimal("35.1796"))
                .longitude(new BigDecimal("129.0756"))
                .build();

        // when
        Restaurant domain = vo.toDomain();

        // then
        assertThat(domain.getRestaurantId()).isEqualTo("rest-456");
        assertThat(domain.getRestaurantName()).isEqualTo("훌륭한 레스토랑");
        assertThat(domain.getPhone()).isEqualTo("02-9876-5432");
        assertThat(domain.getAddress()).isNotNull();
        assertThat(domain.getAddress().getProvince()).isEqualTo("부산광역시");
        assertThat(domain.getAddress().getCity()).isEqualTo("해운대구");
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("33.4996"),
                new BigDecimal("126.5312")
        );
        Address address = Address.create(
                "제주특별자치도",
                "제주시",
                "노형동",
                "789번지",
                coordinate
        );
        Restaurant original = Restaurant.create(
                "rest-789",
                "제주 맛집",
                "064-1111-2222",
                address
        );

        // when
        Restaurant converted = RestaurantVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}