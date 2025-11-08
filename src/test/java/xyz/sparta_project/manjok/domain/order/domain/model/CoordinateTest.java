package xyz.sparta_project.manjok.domain.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Coordinate Value Object 테스트")
class CoordinateTest {

    @Nested
    @DisplayName("create() - 좌표 생성")
    class CreateCoordinate {

        @Test
        @DisplayName("정상적인 위도/경도 값으로 Coordinate 생성에 성공한다")
        void create_success() {
            // given
            BigDecimal latitude = new BigDecimal("37.5665");
            BigDecimal longitude = new BigDecimal("126.9780");

            // when
            Coordinate coordinate = Coordinate.create(latitude, longitude);

            // then
            assertThat(coordinate.getLatitude()).isEqualTo(latitude);
            assertThat(coordinate.getLongitude()).isEqualTo(longitude);
        }

        @Test
        @DisplayName("위도 또는 경도가 null 이면 예외가 발생한다")
        void create_fail_nullValues() {
            assertThatThrownBy(() ->
                    Coordinate.create(null, new BigDecimal("126.9780"))
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Coordinate.create(new BigDecimal("37.5665"), null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("위도가 -90 ~ 90 범위를 벗어나면 예외가 발생한다")
        void create_fail_invalidLatitude() {
            assertThatThrownBy(() ->
                    Coordinate.create(new BigDecimal("91"), new BigDecimal("126.9780"))
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Coordinate.create(new BigDecimal("-91"), new BigDecimal("126.9780"))
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("경도가 -180 ~ 180 범위를 벗어나면 예외가 발생한다")
        void create_fail_invalidLongitude() {
            assertThatThrownBy(() ->
                    Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("181"))
            ).isInstanceOf(OrderException.class);

            assertThatThrownBy(() ->
                    Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("-181"))
            ).isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("calculateDistanceTo() - 두 좌표 간 거리 계산")
    class CalculateDistance {

        @Test
        @DisplayName("null 좌표를 비교하려 하면 예외가 발생한다")
        void calculateDistance_fail_null() {
            Coordinate c1 = Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("126.9780"));

            assertThatThrownBy(() ->
                    c1.calculateDistanceTo(null)
            ).isInstanceOf(OrderException.class);
        }

        @Test
        @DisplayName("두 좌표 간 거리를 km 단위로 계산한다 (서울시청 ↔ 부산시청 약 325km)")
        void calculateDistance_success() {
            // 서울시청 (37.5665, 126.9780)
            Coordinate seoul = Coordinate.create(new BigDecimal("37.5665"), new BigDecimal("126.9780"));
            // 부산시청 (35.1796, 129.0756)
            Coordinate busan = Coordinate.create(new BigDecimal("35.1796"), new BigDecimal("129.0756"));

            // when
            double distance = seoul.calculateDistanceTo(busan);

            // then
            assertThat(distance).isBetween(320.0, 360.0); // 허용 오차 범위
        }
    }
}
