package xyz.sparta_project.manjok.domain.order.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import xyz.sparta_project.manjok.domain.order.domain.model.Coordinate;

import java.math.BigDecimal;

/**
 * Coordinate Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CoordinateVO {

    @Column(name = "orderer_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "orderer_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    /**
     * 도메인 → VO
     */
    public static CoordinateVO from(Coordinate domain) {
        return CoordinateVO.builder()
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .build();
    }

    /**
     * VO → 도메인
     */
    public Coordinate toDomain() {
        return Coordinate.builder()
                .latitude(this.latitude)
                .longitude(this.longitude)
                .build();
    }
}