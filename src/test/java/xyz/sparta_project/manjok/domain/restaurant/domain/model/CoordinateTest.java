package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Coordinate 값 객체 테스트")
class CoordinateTest {

    @Test
    @DisplayName("유효한 좌표를 생성할 수 있다.")
    void should_create_valid_coordinate() {
        // when
        Coordinate coordinate = Coordinate.of(
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780)
        );

        // then
        assertThat(coordinate.getLatitude()).isEqualTo(BigDecimal.valueOf(37.5665));
        assertThat(coordinate.getLongitude()).isEqualTo(BigDecimal.valueOf(126.9780));
        assertThat(coordinate.hasCoordinate()).isTrue();
    }

    @Test
    @DisplayName("Double 타입으로 좌표를 생성할 수 있다.")
    void should_create_coordinate_with_double() {
        // when
        Coordinate coordinate = Coordinate.of(37.5665, 126.9780);

        // then
        assertThat(coordinate).isNotNull();
        assertThat(coordinate.hasCoordinate()).isTrue();
    }

    @Test
    @DisplayName("위도가 범위를 벗어나면 예외가 발생한다.")
    void should_throw_exception_for_invalid_latitude() {
        // when & then
        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(91), BigDecimal.valueOf(0)))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.INVALID_LATITUDE_RANGE);

        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(-91), BigDecimal.valueOf(0)))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.INVALID_LATITUDE_RANGE);
    }

    @Test
    @DisplayName("경도가 범위를 벗어나면 예외가 발생한다.")
    void should_throw_exception_for_invalid_longitude() {
        // when & then
        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(0), BigDecimal.valueOf(181)))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.INVALID_LONGITUDE_RANGE);

        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(0), BigDecimal.valueOf(-181)))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.INVALID_LONGITUDE_RANGE);
    }

    @Test
    @DisplayName("null 값으로 생성하면 예외가 발생한다.")
    void should_throw_exception_for_null_values() {
        // when & then
        assertThatThrownBy(() -> Coordinate.of((BigDecimal) null, BigDecimal.valueOf(0)))
                .isInstanceOf(RestaurantException.class)
                .hasFieldOrPropertyWithValue("errorCode", RestaurantErrorCode.COORDINATE_REQUIRED);
    }

    @Test
    @DisplayName("두 좌표 간 거리를 계산할 수 있다.")
    void should_calculate_distance_between_coordinates() {
        // given
        Coordinate seoul = Coordinate.of(37.5665, 126.9780);  // 서울시청
        Coordinate busan = Coordinate.of(35.1796, 129.0756);  // 부산시청

        // when
        double distance = seoul.distanceTo(busan);

        // then
        assertThat(distance).isBetween(320.0, 330.0); // 약 325km
    }

    @Test
    @DisplayName("반경 내 좌표인지 확인할 수 있다.")
    void should_check_if_coordinate_is_nearby() {
        // given
        Coordinate gangnam = Coordinate.of(37.4979, 127.0276);
        Coordinate jamsil = Coordinate.of(37.5133, 127.1001);
        Coordinate busan = Coordinate.of(35.1796, 129.0756);

        // when & then
        assertThat(gangnam.isNearby(jamsil, 10)).isTrue();   // 10km 내
        assertThat(gangnam.isNearby(busan, 100)).isFalse();  // 100km 밖
    }

    @Test
    @DisplayName("Google Maps URL을 생성할 수 있다")
    void should_generate_google_maps_url() {
        // given
        Coordinate coordinate = Coordinate.of(37.5665, 126.9780);

        // when
        String url = coordinate.toGoogleMapsUrl();

        // then
        assertThat(url).isEqualTo("https://www.google.com/maps?q=37.5665000,126.9780000");
    }

    @Test
    @DisplayName("네이버 지도 URL을 생성할 수 있다")
    void should_generate_naver_map_url() {
        // given
        Coordinate coordinate = Coordinate.of(37.5665, 126.9780);

        // when
        String url = coordinate.toNaverMapUrl();

        // then
        assertThat(url).isEqualTo("https://map.naver.com/v5/search/37.5665000,126.9780000");
    }

    @Test
    @DisplayName("값 객체는 동등성 비교가 가능하다")
    void should_compare_equality() {
        // given
        Coordinate coord1 = Coordinate.of(37.5665, 126.9780);
        Coordinate coord2 = Coordinate.of(37.5665, 126.9780);
        Coordinate coord3 = Coordinate.of(37.5000, 127.0000);

        // when & then
        assertThat(coord1).isEqualTo(coord2);
        assertThat(coord1).isNotEqualTo(coord3);
        assertThat(coord1.hashCode()).isEqualTo(coord2.hashCode());
    }

}