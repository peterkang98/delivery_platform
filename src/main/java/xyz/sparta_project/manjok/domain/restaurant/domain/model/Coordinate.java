package xyz.sparta_project.manjok.domain.restaurant.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantErrorCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.exception.RestaurantException;

import java.math.BigDecimal;

/**
 * Coordinate value Object
 * - 위도/경도 좌표 정보를 표현하는 불변 객체
 * - 순수 도메인 모델
 * */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Coordinate {

    private BigDecimal latitude;   // 위도 (-90 ~ 90)
    private BigDecimal longitude;  // 경도 (-180 ~ 180)

    /**
     * 좌표 생성 (유효성 검증 포함)
     * */
    public static Coordinate of(BigDecimal latitude, BigDecimal longitude) {
        Coordinate coordinate = new Coordinate(latitude, longitude);
        coordinate.validate();
        return coordinate;
    }

    /**
     * 좌표 생성 (Double 타입)
     * */
    public static Coordinate of(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return of(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }

    /**
     * 좌표 유효성 검증
     * */
    private void validate() {
        if (latitude == null || longitude == null) {
            throw new RestaurantException(RestaurantErrorCode.COORDINATE_REQUIRED);
        }

        // 위도 범위 검증 (-90 ~ 90)
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_LATITUDE_RANGE);
        }
        // 경도 범위 검증 (-180 ~ 180)
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_LONGITUDE_RANGE);
        }
    }

    /**
     * 좌표가 설정되어 있는지 확인
     * */
    public boolean hasCoordinate() {
        return latitude != null && longitude != null;
    }

    /**
     * 두 좌표 간 거리 계산 (Haversine 공식 사용)
     * @param other 다른 좌표
     * @return 거리 (km)
     * */
    public double distanceTo(Coordinate other) {
        if (other == null || !other.hasCoordinate() || !this.hasCoordinate()) {
            throw new RestaurantException(RestaurantErrorCode.INVALID_COORDINATE);
        }

        double earthRadius = 6371.0; // 지구 반지름 (km)

        double lat1Rad = Math.toRadians(this.latitude.doubleValue());
        double lat2Rad = Math.toRadians(other.latitude.doubleValue());
        double deltaLat = Math.toRadians(other.latitude.subtract(this.latitude).doubleValue());
        double deltaLon = Math.toRadians(other.longitude.subtract(this.longitude).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    /**
     * 근처 좌표인지 확인
     * @param other 다른 좌표
     * @param radiusKm 반경
     * @return 반경 내에 있으면 true
     * */
    public boolean isNearby(Coordinate other, double radiusKm) {
        if (other == null || !other.hasCoordinate() || !this.hasCoordinate()) {
            return false;
        }
        return distanceTo(other) <= radiusKm;
    }

    /**
     * 문자열 표현
     * */
    @Override
    public String toString() {
        if (!hasCoordinate()) {
            return "좌표 없음";
        }
        return String.format("위도: %.7f, 경도: %.7f", latitude, longitude);
    }

    /**
     * Google Maps URL 생성
     */
    public String toGoogleMapsUrl() {
        if (!hasCoordinate()) {
            return null;
        }
        return String.format("https://www.google.com/maps?q=%.7f,%.7f",
                latitude, longitude);
    }

    /**
     * 네이버 지도 URL 생성
     */
    public String toNaverMapUrl() {
        if (!hasCoordinate()) {
            return null;
        }
        return String.format("https://map.naver.com/v5/search/%.7f,%.7f",
                latitude, longitude);
    }
}
