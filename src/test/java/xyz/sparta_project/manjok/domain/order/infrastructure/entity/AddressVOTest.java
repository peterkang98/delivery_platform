package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.Address;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AddressVO Value Object 변환 테스트")
class AddressVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );
        Address domain = Address.create(
                "서울특별시",
                "중구",
                "명동",
                "123번지",
                coordinate
        );

        // when
        AddressVO vo = AddressVO.from(domain);

        // then
        assertThat(vo.getProvince()).isEqualTo("서울특별시");
        assertThat(vo.getCity()).isEqualTo("중구");
        assertThat(vo.getDistrict()).isEqualTo("명동");
        assertThat(vo.getDetailAddress()).isEqualTo("123번지");
        assertThat(vo.getCoordinate()).isNotNull();
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        CoordinateVO coordinateVO = CoordinateVO.builder()
                .latitude(new BigDecimal("35.1796"))
                .longitude(new BigDecimal("129.0756"))
                .build();

        AddressVO vo = AddressVO.builder()
                .province("부산광역시")
                .city("해운대구")
                .district("우동")
                .detailAddress("456번지")
                .coordinate(coordinateVO)
                .build();

        // when
        Address domain = vo.toDomain();

        // then
        assertThat(domain.getProvince()).isEqualTo("부산광역시");
        assertThat(domain.getCity()).isEqualTo("해운대구");
        assertThat(domain.getDistrict()).isEqualTo("우동");
        assertThat(domain.getDetailAddress()).isEqualTo("456번지");
        assertThat(domain.getCoordinate()).isNotNull();
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        Coordinate coordinate = Coordinate.create(
                new BigDecimal("33.4996"),
                new BigDecimal("126.5312")
        );
        Address original = Address.create(
                "제주특별자치도",
                "제주시",
                "노형동",
                "789번지",
                coordinate
        );

        // when
        Address converted = AddressVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}