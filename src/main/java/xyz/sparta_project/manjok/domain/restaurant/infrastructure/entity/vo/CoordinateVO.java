package xyz.sparta_project.manjok.domain.restaurant.infrastructure.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import xyz.sparta_project.manjok.domain.restaurant.domain.model.Coordinate;

import java.math.BigDecimal;

/**
 * Coordinate Value Object (Embeddable)
 * - 위도/경도 좌표 정보를 표현하는 불변 객체
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class CoordinateVO {

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;   // 위도 (-90 ~ 90)

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;  // 경도 (-180 ~ 180)

    // ==================== 도메인 ↔ VO 변환 ====================

    /**
     * 도메인 모델을 VO로 변환
     */
    public static CoordinateVO fromDomain(Coordinate domain) {
        if (domain == null) {
            return null;
        }
        return CoordinateVO.builder()
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .build();
    }

    /**
     * VO를 도메인 모델로 변환
     */
    public Coordinate toDomain() {
        return Coordinate.builder()
                .latitude(this.latitude)
                .longitude(this.longitude)
                .build();
    }
}