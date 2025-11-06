package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CoordinateVO Value Object 변환 테스트")
class CoordinateVOTest {

    @Test
    @DisplayName("도메인 -> VO 변환이 정상적으로 이루어진다")
    void fromDomain_success() {
        // given
        Coordinate domain = Coordinate.create(
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780")
        );

        // when
        CoordinateVO vo = CoordinateVO.from(domain);

        // then
        assertThat(vo.getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665"));
        assertThat(vo.getLongitude()).isEqualByComparingTo(new BigDecimal("126.9780"));
    }

    @Test
    @DisplayName("VO -> 도메인 변환이 정상적으로 이루어진다")
    void toDomain_success() {
        // given
        CoordinateVO vo = CoordinateVO.builder()
                .latitude(new BigDecimal("35.1796"))
                .longitude(new BigDecimal("129.0756"))
                .build();

        // when
        Coordinate domain = vo.toDomain();

        // then
        assertThat(domain.getLatitude()).isEqualByComparingTo(new BigDecimal("35.1796"));
        assertThat(domain.getLongitude()).isEqualByComparingTo(new BigDecimal("129.0756"));
    }

    @Test
    @DisplayName("도메인 -> VO -> 도메인 변환 시 값이 동일해야 한다")
    void roundTripConversion() {
        // given
        Coordinate original = Coordinate.create(
                new BigDecimal("33.4996"),
                new BigDecimal("126.5312")
        );

        // when
        Coordinate converted = CoordinateVO.from(original).toDomain();

        // then
        assertThat(converted)
                .usingRecursiveComparison()
                .isEqualTo(original);
    }
}