package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Coordinate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CoordinateVO 변환 테스트
 */
class CoordinateVOTest {

    @Test
    @DisplayName("도메인 Coordinate를 CoordinateVO로 변환")
    void fromDomain_ShouldConvertCoordinateToCoordinateVO() {
        // given
        Coordinate domain = Coordinate.builder()
                .latitude(new BigDecimal("37.5665350"))
                .longitude(new BigDecimal("126.9779690"))
                .build();

        // when
        CoordinateVO vo = CoordinateVO.fromDomain(domain);

        // then
        assertThat(vo).isNotNull();
        assertThat(vo.getLatitude()).isEqualByComparingTo(new BigDecimal("37.5665350"));
        assertThat(vo.getLongitude()).isEqualByComparingTo(new BigDecimal("126.9779690"));
    }

    @Test
    @DisplayName("CoordinateVO를 도메인 Coordinate로 변환")
    void toDomain_ShouldConvertCoordinateVOToCoordinate() {
        // given
        CoordinateVO vo = CoordinateVO.builder()
                .latitude(new BigDecimal("35.1796000"))
                .longitude(new BigDecimal("129.0756000"))
                .build();

        // when
        Coordinate domain = vo.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.getLatitude()).isEqualByComparingTo(new BigDecimal("35.1796000"));
        assertThat(domain.getLongitude()).isEqualByComparingTo(new BigDecimal("129.0756000"));
    }
}