package xyz.sparta_project.manjok.domain.order.domain.model;

import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderErrorCode;
import xyz.sparta_project.manjok.domain.order.domain.exception.OrderException;

import java.math.BigDecimal;

/**
 * Coordinate (Value Object)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"latitude", "longitude"})
public class Coordinate {

    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");

    private BigDecimal latitude;
    private BigDecimal longitude;

    /**
     * 팩토리 메서드 - 좌표 생성
     */
    public static Coordinate create(BigDecimal latitude, BigDecimal longitude) {
        validateCoordinate(latitude, longitude);

        return Coordinate.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    /**
     * 좌표 유효성 검증
     */
    private static void validateCoordinate(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new OrderException(OrderErrorCode.INVALID_COORDINATE, "위도와 경도는 필수입니다.");
        }

        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new OrderException(OrderErrorCode.INVALID_COORDINATE,
                    "위도는 -90에서 90 사이여야 합니다.");
        }

        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new OrderException(OrderErrorCode.INVALID_COORDINATE,
                    "경도는 -180에서 180 사이여야 합니다.");
        }
    }

    /**
     * 두 좌표 간의 거리 계산 (Haversine formula - km 단위)
     */
    public double calculateDistanceTo(Coordinate other) {
        if (other == null) {
            throw new OrderException(OrderErrorCode.INVALID_COORDINATE, "비교할 좌표가 null입니다.");
        }

        final int EARTH_RADIUS = 6371; // km

        double lat1 = Math.toRadians(this.latitude.doubleValue());
        double lat2 = Math.toRadians(other.latitude.doubleValue());
        double lon1 = Math.toRadians(this.longitude.doubleValue());
        double lon2 = Math.toRadians(other.longitude.doubleValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}